package hello.jdbc.connection;

public abstract class ConnectionConst {

    // abstract 로 객체 생성 못하게 막기.
    public static final String URL = "jdbc:h2:tcp://localhost/~/jdbc";
    public static final String USERNAME = "sa";
    public static final String PASSWORD = "sa";
}
