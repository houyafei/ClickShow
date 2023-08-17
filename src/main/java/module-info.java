module com.lightmatter.clickshow {

    requires javafx.controls;
    requires javafx.fxml;

    requires org.kordamp.bootstrapfx.core;
    requires jnativehook;
    requires java.logging;
    requires java.sql;
    requires org.xerial.sqlitejdbc;
    requires shortcutMachine;
    opens com.lightmatter.clickshow to javafx.fxml;
    exports com.lightmatter.clickshow;
}