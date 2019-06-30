package com.boris.users;

import com.mysql.cj.jdbc.MysqlDataSource;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.List;

public class UserDAOImpl implements IUserDAO {


    private MysqlDataSource dataSource;
    private static UserDAOImpl instance;

    private UserDAOImpl(MysqlDataSource dataSource){
        this.dataSource = dataSource;
    }




    public static UserDAOImpl getInstance(){
        if (instance==null){
            //create datasource
            MysqlDataSource dataSource = new MysqlDataSource();
            dataSource.setUser("root");
            dataSource.setPassword("");
            dataSource.setDatabaseName("users_db");

            dataSource.setUrl("jdbc:mysql://localhost:3306/users_db");
            //inject DS
            //create BikeDAOImpl
            instance = new UserDAOImpl(dataSource);
        }
        return instance;
    }


    @Override
    public List<User> getUserList() {
        return null;
    }

    @Override
    public User findUserById(String id) {
        try (Connection connection = dataSource.getConnection()){

            PreparedStatement ps = connection.prepareStatement("SELECT user_id, username, email,  FROM users WHERE user_id=?");
            ps.setString(1, id);
            ResultSet rs = ps.executeQuery();
            if (rs.next()){
                String userId = rs.getString("user_id");
                String username = rs.getString("username");
                String email = rs.getString("email");
                User user= new User(id, username,email );
                return user;
            } else{
                return null;
            }

        } catch (Exception ex){
            System.out.println(ex.getLocalizedMessage());
            return null;
        }    }

    @Override
    public void add(User user) {


        try (Connection connection = dataSource.getConnection()){

            PreparedStatement ps = connection.prepareStatement("INSERT INTO users (user_id,username, email) VALUES (?,?,?)");
            ps.setString(1, user.getUserId());
            ps.setString(2, user.getUsername());
            ps.setString(3, user.getEmail());
            ps.executeUpdate();

        } catch (Exception ex){
            System.out.println(ex.getLocalizedMessage());
        }


    }

    @Override
    public void remove(String userId) {
        try (Connection connection = dataSource.getConnection()){

            PreparedStatement ps = connection.prepareStatement("DELETE FROM users WHERE user_id=?");
            ps.setString(1, userId);
            ps.executeUpdate();

        } catch (Exception ex){
            System.out.println(ex.getLocalizedMessage());
        }
    }

    @Override
    public void update(User user) {


        try (Connection connection = dataSource.getConnection()){

            PreparedStatement ps = connection.prepareStatement("UPDATE username, email WHERE user_id=?");
            ps.setString(1, user.getUserId());
            ps.setString(2, user.getUsername());
            ps.setString(3, user.getEmail());
            ps.executeUpdate();


        } catch (Exception ex){
            System.out.println(ex.getLocalizedMessage());
        }


    }
}
