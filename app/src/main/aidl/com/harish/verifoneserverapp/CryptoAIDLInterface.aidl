// CryptoAIDLInterface.aidl
package com.harish.verifoneserverapp;

parcelable Dummy;


interface CryptoAIDLInterface {

    String encrypt(String textToEncrypt);

    String decrypt(String textToDecrypt);

}