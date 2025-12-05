module kth.library {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.sql;

    opens kth.library to javafx.fxml;          // FXML får skapa HelloController, main-klass etc
    opens kth.library.model to javafx.base;   // JavaFX får läsa Book/Author/Genre för TableView
    
    exports kth.library;
}