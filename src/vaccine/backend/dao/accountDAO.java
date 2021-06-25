package vaccine.backend.dao;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import vaccine.backend.util.SqliteDBCon;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import vaccine.backend.classes.Account;

/**
 * Java Class that contains methods used for CRUD functionalities
 * involving the user_info relation in the database.
 */
public class accountDAO {
    static Connection conn = null;
    static PreparedStatement ps = null;
    static ResultSet rs = null;

    /**
     * Inserts a new tuple in user_info table.
     * @param u Account Bean object
     * @return userID of the inserted record inside the database.
     * If the returned value is -1, raise an error.
     */
    public static int addAccount(Account u) {
        int id = -1;
        try {
            conn = SqliteDBCon.Connector();
            ps = conn.prepareStatement("insert into user_info (username, password, usertype) VALUES (?,?,?)");
            ps.setString(1, u.getUsername());
            ps.setString(2, u.getPassword());
            ps.setString(3, u.getUsertype());
            ps.executeUpdate();
            id = getUserIDByUsername(u.getUsername());
            conn.close();
        } catch (Exception exception) {
            exception.printStackTrace();
        }
        return id;
    }

    /**
     * Updates attributes of an existing tuple in user_info table.
     * @param u Account Bean object
     * @return status - If status > 0, SQL query is successful.
     */
    public static int updateAccount (Account u) {
        int status = 0;
        try {
            conn = SqliteDBCon.Connector();
            ps = conn.prepareStatement("update user_info set username = ?, password = ?, usertype = ? where userID = ?");
            ps.setString(1, u.getUsername());
            ps.setString(2, u.getPassword());
            ps.setString(3, u.getUsertype());
            ps.setInt(4, u.getUserID());
            status = ps.executeUpdate();
            conn.close();
        } catch (Exception exception) {
            exception.printStackTrace();
        }
        return status;
    }

    /**
     * Deletes a tuple in user_info table.
     * @param u Account Bean object
     * @return status - If status > 0, SQL query is successful.
     */
    public static int deleteAccount (Account u) {
        int status = 0;
        try {
            conn = SqliteDBCon.Connector();
            ps = conn.prepareStatement("delete from user_info where userID = ?");
            ps.setInt(1, u.getUserID());
            status = ps.executeUpdate();
            conn.close();
        } catch (Exception exception) {
            exception.printStackTrace();
        }
        return status;
    }

    /**
     * Validates if a given Account object exists inside the database.
     * @param u Account Bean object - Username and Password
     * @return Account Bean object - Username, Password, and Usertype
     */
    public static Account validate(Account u) {
        String user = u.getUsername();
        String pass = u.getPassword();
        try {
            conn = SqliteDBCon.Connector();
            ps = conn.prepareStatement("select * from user_info where username = ? and password = ?");
            ps.setString(1, user);
            ps.setString(2, pass);
            rs = ps.executeQuery();
            if (rs.next()) {
                String username = rs.getString("username");
                String password = rs.getString("password");
                if(user.equals(username) && pass.equals(password)){
                    System.out.println("Success");
                    u.setUserID(rs.getInt("userID"));
                    u.setUsertype(rs.getString("usertype"));
                    u.setUsername(username);
                    u.setPassword(password);
                } else {
                    System.out.println("Failed");
                    return null;
                }
            }
            conn.close();
        } catch (Exception exception) {
            exception.printStackTrace();
        }
        return u;
    }

    /**
     * Returns a userID from user_info table based on the given username
     * @param username
     * @return userID - if returned id is 0, no record is found.
     */
    public static int getUserIDByUsername(String username) {
        int id = 0;
        try {
            conn = SqliteDBCon.Connector();
            ps = conn.prepareStatement("select userID from user_info where username = ?");
            ps.setString(1, username);
            rs = ps.executeQuery();
            while (rs.next())
                id = rs.getInt("userID");
            conn.close();
        } catch (Exception exception) {
            exception.printStackTrace();
        }
        return id;

    }

    /**
     * Method used for getting all tuples from user_info table.
     * @return ObservableList of Account Bean objects.
     * if null, the user_info table is empty or the query failed to execute.
     */
    public static ObservableList<Account> getAllAccounts() {
        ObservableList<Account> list = FXCollections.observableArrayList();
        try {
            conn = SqliteDBCon.Connector();
            ps = conn.prepareStatement("select * from user_info");
            rs = ps.executeQuery();
            while (rs.next()) {
                list.add(new Account(rs.getInt("userID"),
                        rs.getString("username"),
                        rs.getString("password"),
                        rs.getString("usertype")
                ));
            }
        } catch (Exception exception) {
            exception.printStackTrace();
        }
        return list;
    }
}
