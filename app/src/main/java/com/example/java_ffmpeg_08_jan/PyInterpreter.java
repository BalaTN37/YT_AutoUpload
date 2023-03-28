package com.example.java_ffmpeg_08_jan;
import org.python.util.PythonInterpreter;

public class PyInterpreter {

    public static void printtemp(String[] args){

        PythonInterpreter pythonInterpreter = new PythonInterpreter();
        pythonInterpreter.exec(" print('It is hot today')");
    }
}