package com.boris.users;

import com.mysql.cj.jdbc.MysqlDataSource;

import java.sql.Array;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

public class UserDAOImpl implements IUserDAO {


    private static UserDAOImpl instance;
    private MysqlDataSource dataSource;
    private List<User> userList = new ArrayList<>();
    private List users = new ArrayList();

    private UserDAOImpl(MysqlDataSource dataSource) {
        this.dataSource = dataSource;
    }


    public static UserDAOImpl getInstance() {
        if (instance == null) {
            //create datasource
            MysqlDataSource dataSource = new MysqlDataSource();
            dataSource.setUser("root");
            dataSource.setPassword("");
            dataSource.setDatabaseName("vertx_users_db");

            dataSource.setUrl("jdbc:mysql://localhost/vertx_users_db?useUnicode=true&useJDBCCompliantTimezoneShift=true&useLegacyDatetimeCode=false&serverTimezone=UTC");

            //inject DS
            //create BikeDAOImpl
            instance = new UserDAOImpl(dataSource);
        }
        return instance;
    }


    @Override
    public List<User> getUserList() {
        try (Connection connection = dataSource.getConnection()) {


            PreparedStatement ps = connection.prepareStatement("SELECT * FROM users ");
            ResultSet rs = ps.executeQuery();
            String id;
            String username;
            String email;

            while ( rs.next() ) {


                id = rs.getString("id");
                username = rs.getString("username");
                email = rs.getString("email");
                userList.add(new User(id,username, email));

            }
            if (userList != null) {

                return userList;
            } else {
                return null;
            }

        } catch (Exception ex) {
            System.out.println(ex.getLocalizedMessage());
            return null;
        }
    }


    @Override
    public User findUserById(String id) {
        try (Connection connection = dataSource.getConnection()) {

            PreparedStatement ps = connection.prepareStatement("SELECT id, username, email FROM users WHERE id=?");
            ps.setString(1, id);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                String userId = rs.getString("id");
                String username = rs.getString("username");
                String email = rs.getString("email");
                User user = new User(id, username, email);
                return user;
            } else {
                return null;
            }

        } catch (Exception ex) {
            System.out.println(ex.getLocalizedMessage());
            return null;
        }
    }

    @Override
    public void add(User user) {


        try (Connection connection = dataSource.getConnection()) {

            PreparedStatement ps = connection.prepareStatement("INSERT INTO users (id,username, email) VALUES (?,?,?)");
            ps.setString(1, user.getId());
            ps.setString(2, user.getUsername());
            ps.setString(3, user.getEmail());
            ps.executeUpdate();

        } catch (Exception ex) {
            System.out.println(ex.getLocalizedMessage());
        }


    }

    @Override
    public void remove(String user_id) {
        try (Connection connection = dataSource.getConnection()) {

            PreparedStatement ps = connection.prepareStatement("DELETE FROM users WHERE id=?");
            ps.setString(1, user_id);
            ps.executeUpdate();

        } catch (Exception ex) {
            System.out.println(ex.getLocalizedMessage());
        }
    }

    @Override
    public void update(User user) {


        try (Connection connection = dataSource.getConnection()) {

            PreparedStatement ps = connection.prepareStatement("UPDATE username, email WHERE id=?");
            ps.setString(1, user.getId());
            ps.setString(2, user.getUsername());
            ps.setString(3, user.getEmail());
            ps.executeUpdate();


        } catch (Exception ex) {
            System.out.println(ex.getLocalizedMessage());
        }


    }
}
