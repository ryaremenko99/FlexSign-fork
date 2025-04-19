/*
 * Copyright (c) 2021, The UAPKI Project Authors.
 * 
 * Redistribution and use in source and binary forms, with or without 
 * modification, are permitted provided that the following conditions are 
 * met:
 * 
 * 1. Redistributions of source code must retain the above copyright 
 * notice, this list of conditions and the following disclaimer.
 * 
 * 2. Redistributions in binary form must reproduce the above copyright 
 * notice, this list of conditions and the following disclaimer in the 
 * documentation and/or other materials provided with the distribution.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS 
 * IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED 
 * TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A 
 * PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT 
 * HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, 
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED 
 * TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR 
 * PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF 
 * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING 
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS 
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package com.sit.uapki;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.sit.uapki.cert.CertId;
import com.sit.uapki.common.Document;
import com.sit.uapki.common.PkiData;
import com.sit.uapki.common.PkiOid;
import com.sit.uapki.common.PkiTime;
import com.sit.uapki.crl.CrlId;
import com.sit.uapki.key.KeyId;
import com.sit.uapki.key.KeyInfo;
import com.sit.uapki.key.StorageInfo;
import com.sit.uapki.method.AddCert;
import com.sit.uapki.method.AddCrl;
import com.sit.uapki.method.CertInfo;
import com.sit.uapki.method.ChangePassword;
import com.sit.uapki.method.Close;
import com.sit.uapki.method.CreateKey;
import com.sit.uapki.method.CrlInfo;
import com.sit.uapki.method.Decrypt;
import com.sit.uapki.method.Deinit;
import com.sit.uapki.method.DeleteKey;
import com.sit.uapki.method.Digest;
import com.sit.uapki.method.GetCert;
import com.sit.uapki.method.GetCsr;
import com.sit.uapki.method.Init;
import com.sit.uapki.method.InitKeyUsage;
import com.sit.uapki.method.Keys;
import com.sit.uapki.method.ListCerts;
import com.sit.uapki.method.Open;
import com.sit.uapki.method.Providers;
import com.sit.uapki.method.RemoveCert;
import com.sit.uapki.method.SelectKey;
import com.sit.uapki.method.Sign;
import com.sit.uapki.method.Storages;
import com.sit.uapki.method.Verify;
import com.sit.uapki.method.VerifyCert;
import com.sit.uapki.method.Version;
import com.sun.jna.Native;
import com.sun.jna.Platform;
import com.sun.jna.Pointer;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;


@Slf4j
public final class Library {

    private interface UapkiNativeInterface extends com.sun.jna.Library {
        public Pointer process (String str); 
        public void json_free (Pointer str);
    }
    
    private final UapkiNativeInterface uapki;
    private final Gson gson;
    private String name;
    private String version;

    public Library () throws UapkiException {
        this(true);
    }

    public Library (boolean enableDebugOut) throws UapkiException {
        try {
            LoaderLibFromResource loader = new LoaderLibFromResource();

            //  Warning: here need order
            loader.load("uapkic", "2.0.1");
            loader.load("uapkif", "2.0.1");
            loader.load("uapki", null);

            //  Load cm-providers
            final String[] CM_LIBNAMES = new String[] {
                "cm-pkcs12.1.0.13"
            };
            for (int i = 0; i < CM_LIBNAMES.length; i++) {
                loader.load(CM_LIBNAMES[i], null);
            }

            log.info("Loading native libraries...");
            System.setProperty("jna.library.path", "/tmp/uapki");
            this.uapki = Native.load("uapki", UapkiNativeInterface.class);
        }
        catch (Exception e) {
            log.error("Can't load native library: ");
            throw new RuntimeException(e);
//            throw new UapkiException("Can't load native library: " + e.getMessage(), -1, "");
        }

        this.gson = new Gson();

        Version.Result version_result = version();
        this.version = version_result.getVersion();
        this.name = version_result.getName();
    }

    public String getVersion () {
        return version;
    }
    
    public String getName () {
        return name;
    }
    
    /**
     * Send JSON-request to UAPKI and receive JSON-response from UAPKI.
     * @param request (JSON-string) to UAPKI
     * @return JSON-string from UAPKI
     * @throws UapkiException
     */
    public String processJson (String request) throws UapkiException {
        try {
            Pointer result = uapki.process(request);
            String ret = result.getString(0);
            uapki.json_free(result);
            return ret;
        }
        catch(Exception e) {
            throw new UapkiException("Native library exception: " + e.getMessage(), -2, request);
        }
    }
    
    private class LoaderLibFromResource {
        final String EXT_SO = ".so";
        final String EXT_DLL = ".dll";
        final String EXT_DYLIB = ".dylib";

        final String libExt;
        final String libPrefix;
        final String resPath = "/natives/" + Platform.RESOURCE_PREFIX + "/";

        LoaderLibFromResource () {
            libPrefix = (Platform.isWindows()) ? "" : "lib";
            if (Platform.isWindows()) libExt = EXT_DLL;
            else if (Platform.isMac()) libExt = EXT_DYLIB;
            else libExt = EXT_SO;
            log.info(" resPath: '" + resPath + "'\n libPrefix: '" + libPrefix + "'\n libExt: '" + libExt + "'");
        }
        
        void load (String libName, String version) throws IOException {
            try {
                String file_ext = libExt;
                if (version != null) {
                    if (libExt == EXT_SO) {
                        file_ext = libExt + '.' + version;
                    }
                    else if (libExt == EXT_DYLIB) {
                        file_ext = '.' + version + libExt;
                    }
                }

                final String filename = libPrefix + libName + file_ext;

                // Have to use a stream
                log.info("Load from resource: '" + (resPath + filename) + "'");
                InputStream in = getClass().getResourceAsStream(resPath + filename);

                // Always write to different location
                File tmp_file = new File(System.getProperty("java.io.tmpdir") + "/uapki/" + filename);
                OutputStream out = FileUtils.openOutputStream(tmp_file);
                IOUtils.copy(in, out);
                in.close();
                out.close();
                System.load(tmp_file.toString());
                log.info(("Loaded from tmp: '" + tmp_file + "'"));
            }
            catch (UnsatisfiedLinkError e) {
                System.out.println("LoaderLibFromResource.load: " + e.getMessage());
            }
        }
    }
    
    private class AnyRequest {
        public String method;
        public JsonElement parameters;
    }
    
    private class AnyResponse {
        public int errorCode;
        public String error;
        public String method;
        public JsonElement result;
    }

    private JsonElement processGson (String method, JsonElement params) throws UapkiException {
        AnyRequest req = new AnyRequest();
        req.method = method;
        req.parameters = params;
        String jsonRequest = gson.toJson(req);
        log.info("jsonRequest: " + jsonRequest);
        String jsonResponse = processJson(jsonRequest);
        log.info("jsonResponse: " + jsonResponse);
        AnyResponse response = gson.fromJson(jsonResponse, AnyResponse.class);

        if (response.errorCode != 0) {
            throw new UapkiException(response.error, response.errorCode, response.method);
        }

        return response.result;
    }

    public Version.Result version () throws UapkiException {
        return gson.fromJson(processGson(Version.METHOD, null), Version.Result.class);
    }

    public Init.Result init (Init.Parameters params) throws UapkiException {
        return gson.fromJson(processGson(Init.METHOD, gson.toJsonTree(params)), Init.Result.class);
    }

    public void deinit () throws UapkiException {
        processGson(Deinit.METHOD, null);
    }

    public ArrayList<Providers.CmProviderInfo> getProviders () throws UapkiException {
        return gson.fromJson(processGson(Providers.METHOD, null), Providers.Result.class).getProviders();
    }

    public ArrayList<Storages.StorageInfo> getStorages (String providerId) throws UapkiException {
        return gson.fromJson(processGson(Storages.METHOD, gson.toJsonTree(new Storages.Parameters(providerId))), Storages.Result.class).getStorages();
    }

    public StorageInfo openStorage (String providerId, String storageId, String password, Open.Mode mode) throws UapkiException {
        return gson.fromJson(processGson(Open.METHOD, gson.toJsonTree(new Open.Parameters(providerId, storageId, null, password, mode, null))), Open.Result.class);
    }

    public StorageInfo openStorage (String providerId, String storageId, String password, Open.Mode mode, String openParams) throws UapkiException {
        return gson.fromJson(processGson(Open.METHOD, gson.toJsonTree(new Open.Parameters(providerId, storageId, null, password, mode, openParams))), Open.Result.class);
    }

    public StorageInfo openStorage (String providerId, String storageId, String username, String password, Open.Mode mode) throws UapkiException {
        return gson.fromJson(processGson(Open.METHOD, gson.toJsonTree(new Open.Parameters(providerId, storageId, username, password, mode, null))), Open.Result.class);
    }

    public StorageInfo openStorage (String providerId, String storageId, String username, String password, Open.Mode mode, String openParams) throws UapkiException {
        return gson.fromJson(processGson(Open.METHOD, gson.toJsonTree(new Open.Parameters(providerId, storageId, username, password, mode, openParams))), Open.Result.class);
    }

    public void closeStorage () throws UapkiException {
        processGson(Close.METHOD, null);
    }
    
    public ArrayList<KeyInfo> getKeys () throws UapkiException {
        return gson.fromJson(processGson(Keys.METHOD, null), Keys.Result.class).getKeys();
    }

    public SelectKey.Result selectKey (KeyId keyId) throws UapkiException {
        return gson.fromJson(processGson(SelectKey.METHOD, gson.toJsonTree(new SelectKey.Parameters(keyId))), SelectKey.Result.class);
    }
    
    public KeyId createKey (PkiOid mechanismId, PkiOid parameterId, String label) throws UapkiException {
        return gson.fromJson(processGson(CreateKey.METHOD, gson.toJsonTree(new CreateKey.Parameters(mechanismId, parameterId, label))), CreateKey.Result.class).getId();
    }

    public KeyId createKey (PkiOid mechanismId, int bits, String label) throws UapkiException {
        return gson.fromJson(processGson(CreateKey.METHOD, gson.toJsonTree(new CreateKey.Parameters(mechanismId, bits, label))), CreateKey.Result.class).getId();
    }

    public void deleteKey (KeyId keyId) throws UapkiException {
        processGson(DeleteKey.METHOD, gson.toJsonTree(new DeleteKey.Parameters(keyId)));
    }
    
    public PkiData getCsr () throws UapkiException {
        return gson.fromJson(processGson(GetCsr.METHOD, null), GetCsr.Result.class).getBytes();
    }
    
    public void changePassword (String password, String newPassword) throws UapkiException {
        processGson(ChangePassword.METHOD, gson.toJsonTree(new ChangePassword.Parameters(password, newPassword)));
    }

    public void initKeyUsage (InitKeyUsage.Type type, String value) throws UapkiException {
        processGson(InitKeyUsage.METHOD, gson.toJsonTree(new InitKeyUsage.Parameters(type, value)));
    }

    public ArrayList<Document> sign (Sign.Parameters params) throws UapkiException {
        return gson.fromJson(processGson(Sign.METHOD, gson.toJsonTree(params)), Sign.Result.class).getSignatures();
    }

    public ArrayList<Document> sign (Sign.SignParams signParams, ArrayList<Sign.DataTbs> dataTbs) throws UapkiException {
        return gson.fromJson(processGson(Sign.METHOD, gson.toJsonTree(new Sign.Parameters(signParams, dataTbs))), Sign.Result.class).getSignatures();
    }

    public Verify.Result verify (PkiData signedData) throws UapkiException {
        return gson.fromJson(processGson(Verify.METHOD, gson.toJsonTree(new Verify.Parameters(signedData, null, false))), Verify.Result.class);
    }

    public Verify.Result verify (PkiData signedData, PkiData content) throws UapkiException {
        return gson.fromJson(processGson(Verify.METHOD, gson.toJsonTree(new Verify.Parameters(signedData, content, false))), Verify.Result.class);
    }

    public Verify.Result verify (PkiData signedData, PkiData content, boolean isDigest) throws UapkiException {
        return gson.fromJson(processGson(Verify.METHOD, gson.toJsonTree(new Verify.Parameters(signedData, content, isDigest))), Verify.Result.class);
    }

    public ArrayList<AddCert.AddedCertInfo> addCert (ArrayList<PkiData> certificates, boolean permanent) throws UapkiException {
        return gson.fromJson(processGson(AddCert.METHOD, gson.toJsonTree(new AddCert.Parameters(certificates, permanent))), AddCert.Result.class).getAdded();
    }

    public ArrayList<AddCert.AddedCertInfo> addCert (PkiData bundle, boolean permanent) throws UapkiException {
        return gson.fromJson(processGson(AddCert.METHOD, gson.toJsonTree(new AddCert.Parameters(bundle, permanent))), AddCert.Result.class).getAdded();
    }

    public CertInfo.Result certInfo (PkiData bytes) throws UapkiException {
        return gson.fromJson(processGson(CertInfo.METHOD, gson.toJsonTree(new CertInfo.Parameters(bytes))), CertInfo.Result.class);
    }

    public CertInfo.Result certInfo (CertId certId) throws UapkiException {
        return gson.fromJson(processGson(CertInfo.METHOD, gson.toJsonTree(new CertInfo.Parameters(certId))), CertInfo.Result.class);
    }

    public PkiData getCert (CertId certId) throws UapkiException {
        return gson.fromJson(processGson(GetCert.METHOD, gson.toJsonTree(new GetCert.Parameters(certId))), GetCert.Result.class).getBytes();
    }

    public ArrayList<CertId> listCerts () throws UapkiException {
        return gson.fromJson(processGson(ListCerts.METHOD, gson.toJsonTree(new ListCerts.Parameters())), ListCerts.Result.class).getCertIds();
    }

    public ListCerts.Result listCerts (Integer offset, Integer pageSize) throws UapkiException {
        return gson.fromJson(processGson(ListCerts.METHOD, gson.toJsonTree(new ListCerts.Parameters(offset, pageSize))), ListCerts.Result.class);
    }

    public void removeCert (CertId certId) throws UapkiException {
        processGson(RemoveCert.METHOD, gson.toJsonTree(new RemoveCert.Parameters(certId)));
    }

    public VerifyCert.Result verifyCert (PkiData bytes, VerifyCert.ValidationType validationType) throws UapkiException {
        return gson.fromJson(processGson(VerifyCert.METHOD, gson.toJsonTree(new VerifyCert.Parameters(bytes, null, validationType))), VerifyCert.Result.class);
    }

    public VerifyCert.Result verifyCert (CertId certId, VerifyCert.ValidationType validationType) throws UapkiException {
        return gson.fromJson(processGson(VerifyCert.METHOD, gson.toJsonTree(new VerifyCert.Parameters(null, certId, validationType))), VerifyCert.Result.class);
    }

    public VerifyCert.Result verifyCert (PkiData bytes, PkiTime validateTime) throws UapkiException {
        return gson.fromJson(processGson(VerifyCert.METHOD, gson.toJsonTree(new VerifyCert.Parameters(bytes, validateTime))), VerifyCert.Result.class);
    }

    public VerifyCert.Result verifyCert (CertId certId, PkiTime validateTime) throws UapkiException {
        return gson.fromJson(processGson(VerifyCert.METHOD, gson.toJsonTree(new VerifyCert.Parameters(certId, validateTime))), VerifyCert.Result.class);
    }

    public AddCrl.Result addCrl (PkiData crl, boolean permanent) throws UapkiException {
        return gson.fromJson(processGson(AddCrl.METHOD, gson.toJsonTree(new AddCrl.Parameters(crl, permanent))), AddCrl.Result.class);
    }

    public CrlInfo.Result crlInfo (PkiData bytes) throws UapkiException {
        return gson.fromJson(processGson(CrlInfo.METHOD, gson.toJsonTree(new CrlInfo.Parameters(bytes))), CrlInfo.Result.class);
    }

    public CrlInfo.Result crlInfo (CrlId crlId) throws UapkiException {
        return gson.fromJson(processGson(CrlInfo.METHOD, gson.toJsonTree(new CrlInfo.Parameters(crlId))), CrlInfo.Result.class);
    }

    public Digest.Result digest (PkiOid hashAlgo, PkiData bytes) throws UapkiException {
        return gson.fromJson(processGson(Digest.METHOD, gson.toJsonTree(new Digest.Parameters(hashAlgo, bytes, null))), Digest.Result.class);
    }

    public Digest.Result digest (PkiOid hashAlgo, String file) throws UapkiException {
        return gson.fromJson(processGson(Digest.METHOD, gson.toJsonTree(new Digest.Parameters(hashAlgo, null, file))), Digest.Result.class);
    }

    public Digest.Result digest (PkiData bytes, PkiOid signAlgo) throws UapkiException {
        return gson.fromJson(processGson(Digest.METHOD, gson.toJsonTree(new Digest.Parameters(bytes, null, signAlgo))), Digest.Result.class);
    }

    public Digest.Result digest (String file, PkiOid signAlgo) throws UapkiException {
        return gson.fromJson(processGson(Digest.METHOD, gson.toJsonTree(new Digest.Parameters(null, file, signAlgo))), Digest.Result.class);
    }
    
    public Decrypt.Result decrypt (PkiData envelopedData) throws UapkiException {
        return gson.fromJson(processGson(Decrypt.METHOD, gson.toJsonTree(new Decrypt.Parameters(envelopedData))), Decrypt.Result.class);
    }
}
