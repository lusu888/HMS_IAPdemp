package com.wz.android.iapdemo.huawei;

public class Key {
    private static final String publicKey = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAkW4zxYG8CgJfwUHUGsxNhCD8yxaRVfTMuI4EUy7Y3D46EWTcJtTvEycj+JjTbf4oKx1qspAAT7G+rIkiYubnPezI2KCEvp4PQmawxG6UB7bnWEiF9TdEXw+CrKONKgMKllhvB3AL39S8B/vPOlYw3cF/aKpcWY6CqlDn1Um6E3O3dfoIJ0iMpOMyzJE7chipZ7zxvL61kpEzlSP1hU2LyfRZtEBocmB6wj3FApdc33ao0wBsWtL7Ulvo5V6gCZrMo+AgYFgv+NKowD6MPWSRFpNqz+as+yfXah/dNTcWieGeFxA7kb8VqOj+HJEH4gjWBSN7ATXEYEkHBOpTLFK+1QIDAQAB";

    /**
     * get the publicKey of the application
     * During the encoding process, avoid storing the public key in clear text.
     * @return
     */
    public static String getPublicKey(){
        return publicKey;
    }
}
