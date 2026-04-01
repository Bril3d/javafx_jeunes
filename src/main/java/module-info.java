module com.coach {
    requires transitive javafx.controls;
    requires javafx.fxml;
    requires atlantafx.base;
    requires org.kordamp.ikonli.javafx;
    requires org.kordamp.ikonli.feather;
    requires transitive java.sql;
    requires java.net.http;
    requires transitive com.google.gson;
    requires jbcrypt;
    requires io.github.cdimascio.dotenv.java;
    requires transitive org.commonmark;

    opens com.coach to javafx.fxml;
    exports com.coach;
    exports com.coach.service;
    exports com.coach.model;
    exports com.coach.repository;
    exports com.coach.ui.view;
    exports com.coach.ui.view.components;
    
    // Allow gson reflection for specific models later
    // opens com.coach.model to com.google.gson;
}
