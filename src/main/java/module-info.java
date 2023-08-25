module com.lightmatter.clickshow {

    requires javafx.controls;
    requires javafx.fxml;

    requires jnativehook;
    requires java.logging;
    requires java.sql;
    requires org.xerial.sqlitejdbc;
    requires shortcutMachine;
    requires org.slf4j;
    opens com.lightmatter.clickshow to javafx.fxml;
    exports com.lightmatter.clickshow;
}