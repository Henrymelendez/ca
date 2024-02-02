# For MLE Client Private Key and Server Public Certificate
openssl pkcs12 -export -in server_cert_7f591161-6b5f-4136-80b8-2ae8a44ad9eb.pem -inkey key_7f591161-6b5f-4136-80b8-2ae8a44ad9eb.pem -out mle.p12 -name mle_alias

# For the additional key and certificate
openssl pkcs12 -export -in cert.pem -inkey key_8g62292-9b2g8293-40b4-4ea4a88da6be.pem -out additional.p12 -name additional_alias


# Import MLE key pair
keytool -importkeystore -destkeystore mykeystore.jks -srckeystore mle.p12 -srcstoretype PKCS12 -alias mle_alias -deststorepass <JKS_Password> -srcstorepass <P12_Password>

# Import the additional key and certificate
keytool -importkeystore -destkeystore mykeystore.jks -srckeystore additional.p12 -srcstoretype PKCS12 -alias additional_alias -deststorepass <JKS_Password> -srcstorepass <P12_Password>


KeyStore keyStore = KeyStore.getInstance(KeyStore.getDefaultType());
try (InputStream is = new FileInputStream("mykeystore.jks")) {
keyStore.load(is, "<JKS_Password>".toCharArray());
}


PrivateKey mlePrivateKey = (PrivateKey) keyStore.getKey("mle_alias", "<P12_Password>".toCharArray());

Certificate mleServerCert = keyStore.getCertificate("mle_alias");
PublicKey mleServerPublicKey = mleServerCert.getPublicKey();

// Similarly, for the additional key/certificate pair
PrivateKey additionalPrivateKey = (PrivateKey) keyStore.getKey("additional_alias", "<P12_Password>".toCharArray());
Certificate additionalCert = keyStore.getCertificate("additional_alias");
PublicKey additionalPublicKey = additionalCert.getPublicKey();
