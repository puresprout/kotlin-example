package com.purestation.app.annotation;

public class ServerConfigExam {
    public static void main(String[] args) {
        System.out.println(ServerConfig.INSTANCE.getProtocol());
        System.out.println(ServerConfig.host);
        System.out.println(ServerConfig.PORT);
    }
}
