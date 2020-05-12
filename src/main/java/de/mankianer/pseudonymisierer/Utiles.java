package de.mankianer.pseudonymisierer;

public class Utiles {

  static public String removeFileExtension(String fileName){
    return fileName.substring(0, fileName.lastIndexOf('.'));
  }

}
