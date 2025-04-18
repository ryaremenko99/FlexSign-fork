package com.flexsolution.sign.config;

import com.flexsolution.sign.model.CMProviderModel;
import com.flexsolution.sign.model.PfxModel;
import com.google.gson.Gson;
import com.sit.uapki.Library;
import com.sit.uapki.UapkiException;
import com.sit.uapki.common.Oids;
import com.sit.uapki.common.PkiData;
import com.sit.uapki.method.Init;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Map;

@Configuration
@Slf4j
public class SignConfig {

    // Кількість ітерацій хешування паролю. Використовується виключно при створенні нового файлу pkcs12.
    // При відкритті/зміненні існуючого використовуються параметри що були при його створенні
    private final static String ITERATIONS = "10000";

    /**
     * Initialize uapki library
     * @param initParameters - initialization parameters
     * @return Library object
     * @throws UapkiException if initParameters are invalid
     */
    @Bean
    protected Library uapkiLibrary(Init.Parameters initParameters) throws UapkiException {
        Library library = new Library(false);
        log.info("Бібліотека завантажена. Назва: " + library.getName() + ", Версія: " + library.getVersion());

        Init.Result init_result = library.init(initParameters);

        log.info("***********************************************");
        log.info("Кільткість сертифікатів в кешу: " + init_result.getCertCacheInfo().getCountCerts());
        log.info("Кількість довірених сертифікатів в кешу: " + init_result.getCertCacheInfo().getCountTrustedCerts());
        log.info("Кількість CRL (Список відкликаних сертифікатів) в кешу: " + init_result.getCrlCacheInfo().getCountCrls());
        log.info("Кількість зареєстрованих CM провайдерів: " + init_result.getCountCmProviders());
        log.info("Оффлай режим: " + init_result.isOffline());
        log.info("TSP (мітка часу) сервер:" + init_result.getTspInfo().getUrl());
        log.info("TSP політика: " + init_result.getTspInfo().getPolicy());
        log.info("***********************************************");

        return library;
    }

    /**
     * Prepare init parameters for uapki library
     *
     * @param cmProviders  Map of CMProverModel objects, where the key is ID of provider and value is a config of provider
     * @param gson  instance of Gson in order to convert java object to json
     * @param certsDirLocation String path to the certs cache directory
     * @param crlsDirLocation String path to the crls cache directory
     * @param tspServer String url to the server that can provide with timestamp confirmation during the signing process
     *
     * @return Init.Parameters
     */
    @Bean
    protected Init.Parameters getInitParameters(Map<String, CMProviderModel> cmProviders,
                                                Gson gson,
                                                @Value("${flex.sign.certsDir}") String certsDirLocation,
                                                @Value("${flex.sign.crlsDir}") String crlsDirLocation,
                                                @Value("${flex.sign.tspServer:http://acsk.privatbank.ua/services/tsp/}") String tspServer) throws IOException {
        // Create folders for cache if they don't exist
        Files.createDirectories(Paths.get(certsDirLocation));
        Files.createDirectories(Paths.get(crlsDirLocation));

        Init.Parameters initParams = new Init.Parameters();

        cmProviders.forEach((key, value) ->
            initParams.addCmProvider(key, gson.toJson(value, CMProviderModel.class)));

        ArrayList<PkiData> list_trustedcerts = new ArrayList<>();
        initParams.setCertCache(certsDirLocation, list_trustedcerts);
        initParams.setCrlCache(crlsDirLocation);
        initParams.setTspParameters(tspServer, null);
        return initParams;
    }

    /**
     * <p>Hardcoded configuration of CMProvider for signatures issued by Privatbank.</p>
     * <p>Please pay attention to the bean's name. It must follow the following pattern: <code>cm-${storage-name}</code></p>
     * <p>The storage name 'pkcs12' allows to sign documents with file storage. Find out more options in documentation of uapki</p>
     *
     * @return CMProviderModel
     */
    @Bean(name = "cm-pkcs12")
    protected CMProviderModel cmPkcs12Provider() {
        PfxModel pfxModel = PfxModel.builder()
                .bagCipher(Oids.CipherAlgo.Aes.AES192_CBC)
                .bagKdf(Oids.HashAlgo.Sha2.HMAC_SHA384)
                .iterations(ITERATIONS)
                .macAlgo(Oids.HashAlgo.Sha2.SHA384)
                .build();

        return CMProviderModel.builder()
                .createPfx(pfxModel)
                .build();
    }
}
