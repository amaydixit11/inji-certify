package io.mosip.certify.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.mosip.certify.core.constants.Constants;
import io.mosip.certify.core.dto.*;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.CachePut;
import org.springframework.data.redis.cache.RedisCache;
import org.springframework.stereotype.Service;
import io.mosip.certify.services.CredentialConfigurationServiceImpl;

import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
public class VCICacheService {

    @Autowired
    private CacheManager cacheManager;

    @Autowired
    private CredentialConfigurationServiceImpl credentialConfigurationService;

    @Autowired
    private AuthorizationServerService authServerService;

    @Autowired
    private ObjectMapper objectMapper;

    @Value("${spring.cache.type:simple}")
    private String cacheType;


    private static final String VCISSUANCE_CACHE = "vcissuance";
    private static final String METADATA_KEY = "metadata";

    @PostConstruct
    public void validateCacheConfiguration() {
        log.info("Cache type configured: {}", cacheType);

        if ("simple".equalsIgnoreCase(cacheType)) {
            log.warn("CRITICAL WARNING: Simple cache configured for production deployment " +
                    "'simple' cache uses in-memory storage isolated to each pod, " +
                    "Multi-pod deployments will experience cache inconsistencies and MAY BREAK FUNCTIONALLY, " +
                    "Current configuration: spring.cache.type=simple (in-memory, non-distributed), " +
                    "Switch to Redis cache for multi-pod deployments, Set spring.cache.type=redis in your configuration ");
        } else if ("redis".equalsIgnoreCase(cacheType)) {
            log.info("Redis cache is configured - suitable for multi-pod deployment");
        } else {
            log.warn("Unknown cache type configured: {}. Please verify configuration.", cacheType);
        }
    }


    @CachePut(value = VCISSUANCE_CACHE, key = "#accessTokenHash")
    public VCIssuanceTransaction setVCITransaction(String accessTokenHash, VCIssuanceTransaction vcIssuanceTransaction) {
        return vcIssuanceTransaction;
    }

    public VCIssuanceTransaction getVCITransaction(String accessTokenHash) {
        Cache cache = cacheManager.getCache(VCISSUANCE_CACHE);
        if (cache == null) {
            log.error("Cache {} not available. Please verify cache configuration.", VCISSUANCE_CACHE);
            return null;
        }
        return cache.get(accessTokenHash, VCIssuanceTransaction.class);
    }

    public void setPreAuthCodeData(String code, PreAuthCodeData data) {
        String key = Constants.PRE_AUTH_CODE_PREFIX + code;
        cacheManager.getCache("preAuthCodeCache").put(key, data);
    }

    public PreAuthCodeData getPreAuthCodeData(String code) {
        String key = Constants.PRE_AUTH_CODE_PREFIX + code;
        Cache.ValueWrapper wrapper = cacheManager.getCache("preAuthCodeCache").get(key);
        return wrapper != null ? (PreAuthCodeData) wrapper.get() : null;
    }

    public CredentialOfferResponse getCredentialOffer(String offerId) {
        String key = Constants.CREDENTIAL_OFFER_PREFIX + offerId;
        Cache cache = cacheManager.getCache("credentialOfferCache");

        if (cache == null) {
            throw new IllegalStateException("credentialOfferCache not available");
        }

        Cache.ValueWrapper wrapper = cache.get(key);
        return wrapper != null ? (CredentialOfferResponse) wrapper.get() : null;
    }

    public void setCredentialOffer(String offerId, CredentialOfferResponse offer) {
        String key = Constants.CREDENTIAL_OFFER_PREFIX + offerId;
        Cache cache = cacheManager.getCache("credentialOfferCache");

        if (cache == null) {
            throw new IllegalStateException("credentialOfferCache not available");
        }

        // For Redis, use RedisCache.put with Duration
        if (cache instanceof RedisCache) {
            ((RedisCache) cache).put(key, offer);
        } else {
            // For simple cache, log warning and use basic put
            log.warn("TTL not supported for cache type: {}. Entry may not expire.", cacheType);
            cache.put(key, offer);
        }
    }

    /**
     * Get issuer metadata from cache. If not present, load from database.
     */
    public Map<String, Object> getIssuerMetadata() {
        Cache cache = cacheManager.getCache("issuerMetadataCache");
        if (cache == null) {
            throw new IllegalStateException("issuerMetadataCache not available");
        }
        Cache.ValueWrapper wrapper = cache.get(METADATA_KEY);

        if (wrapper == null) {
            log.info("Issuer metadata not found in cache, loading from database...");
            try {
                var metadata = credentialConfigurationService.fetchCredentialIssuerMetadata("latest");

                // Convert DTOs to Map structure
                Map<String, Object> metadataMap = new HashMap<>();
                Map<String, Object> credentialConfigsMap = new HashMap<>();

                // Convert each CredentialConfigurationSupportedDTO to Map
                metadata.getCredentialConfigurationSupportedDTO().forEach((configId, configDTO) -> {
                    Map<String, Object> configMap = objectMapper.convertValue(configDTO, Map.class);
                    credentialConfigsMap.put(configId, configMap);
                });
                metadataMap.put(Constants.CREDENTIAL_CONFIGURATIONS_SUPPORTED, credentialConfigsMap);

                List<String> authServers = authServerService.getAllAuthorizationServerUrls();
                if (!authServers.isEmpty()) {
                    metadataMap.put(Constants.AUTHORIZATION_SERVERS, authServers);
                    log.info("Added {} authorization servers to cached metadata", authServers.size());
                }

                // Store in cache
                cache.put(METADATA_KEY, metadataMap);

                log.info("Successfully loaded and cached issuer metadata with {} configurations", credentialConfigsMap.size());

                return metadataMap;
            } catch (Exception e) {
                log.error("Failed to load issuer metadata", e);
                throw new IllegalStateException("Failed to load issuer metadata", e);
            }
        }
        return (Map<String, Object>) wrapper.get();
    }

    public boolean isCodeBlacklisted(String code) {
        String key = "blacklist:" + code;
        Cache.ValueWrapper wrapper = cacheManager.getCache("preAuthCodeCache").get(key);
        return wrapper != null && Boolean.TRUE.equals(wrapper.get());
    }

    /**
     * Blacklist a used pre-authorized code
     */
    public void blacklistPreAuthCode(String code) {
        String key = "blacklist:" + code;
        // Store in cache with same TTL as pre-auth code
        cacheManager.getCache("preAuthCodeCache").put(key, true);

        // Also remove the pre-auth code data
        String codeKey = Constants.PRE_AUTH_CODE_PREFIX + code;
        cacheManager.getCache("preAuthCodeCache").evict(codeKey);

        log.info("Pre-authorized code blacklisted: {}", code);
    }

    /**
     * Store VCI transaction using access token as key
     * Override existing method to accept String key
     */
    @CachePut(value = VCISSUANCE_CACHE, key = "#accessToken")
    public Transaction setTransaction(String accessToken, Transaction vcIssuanceTransaction) {
        log.info("Caching VCI transaction for access token");
        return vcIssuanceTransaction;
    }

    /**
     * Get VCI transaction by access token
     * For use in credential endpoint
     */
    public Transaction getTransactionByToken(String accessToken) {
        return cacheManager.getCache(VCISSUANCE_CACHE).get(accessToken, Transaction.class);
    }

    /**
     * Cache authorization server metadata
     */
    public void setASMetadata(String serverUrl, AuthorizationServerMetadata metadata) {
        String key = Constants.AS_METADATA_PREFIX + serverUrl;
        Cache cache = cacheManager.getCache("asMetadataCache");
        if (cache == null) {
            throw new IllegalStateException("asMetadataCache not available");
        }
        cache.put(key, metadata);
        log.info("Cached AS metadata for: {}", serverUrl);
    }

    /**
     * Get cached authorization server metadata
     */
    public AuthorizationServerMetadata getASMetadata(String serverUrl) {
        String key = Constants.AS_METADATA_PREFIX + serverUrl;
        Cache cache = cacheManager.getCache("asMetadataCache");
        if (cache == null) {
            log.error("Cache {} not available", "asMetadataCache");
            return null;
        }
        Cache.ValueWrapper wrapper = cache.get(key);
        return wrapper != null ? (AuthorizationServerMetadata) wrapper.get() : null;
    }

    /**
     * Evict cached AS metadata for a specific server
     */
    public void evictASMetadata(String serverUrl) {
        String key = Constants.AS_METADATA_PREFIX + serverUrl;
        Cache cache = cacheManager.getCache("asMetadataCache");
        if (cache != null) {
            cache.evict(key);
            log.info("Evicted AS metadata cache for: {}", serverUrl);
        }
    }

    /**
     * Clear all AS metadata cache
     */
    public void clearASMetadataCache() {
        Cache cache = cacheManager.getCache("asMetadataCache");
        if (cache != null) {
            cache.clear();
            log.info("Cleared all AS metadata cache");
        }
    }

    /**
     * Cache credential configuration to AS mapping
     */
    public void setCredentialConfigMapping(String credentialConfigId, String authServerUrl) {
        Cache cache = cacheManager.getCache("credentialConfigMappingCache");
        if (cache == null) {
            throw new IllegalStateException("credentialConfigMappingCache not available");
        }
        cache.put(credentialConfigId, authServerUrl);
        log.debug("Cached credential config mapping: {} -> {}", credentialConfigId, authServerUrl);
    }

    /**
     * Get cached credential configuration to AS mapping
     */
    public String getCredentialConfigMapping(String credentialConfigId) {
        Cache cache = cacheManager.getCache("credentialConfigMappingCache");
        if (cache == null) {
            return null;
        }
        Cache.ValueWrapper wrapper = cache.get(credentialConfigId);
        return wrapper != null ? (String) wrapper.get() : null;
    }
}