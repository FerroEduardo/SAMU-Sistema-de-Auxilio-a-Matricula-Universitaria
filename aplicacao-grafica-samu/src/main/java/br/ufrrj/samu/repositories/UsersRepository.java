package br.ufrrj.samu.repositories;

import java.io.*;
import java.sql.*;
import java.util.*;

import br.ufrrj.samu.entities.*;
import br.ufrrj.samu.exceptions.AlreadyExistsException;
import org.apache.ibatis.jdbc.ScriptRunner;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;


public class UsersRepository {

    private static final Logger LOGGER = LogManager.getLogger(SubjectRepository.class);

    private static UsersRepository instance;

    private Connection connection;

    private BCryptPasswordEncoder encoder;

    private UsersRepository() {
        try {
            LOGGER.debug("Starting connection to database");

            connection = Repository.connection;

            encoder = new BCryptPasswordEncoder(4);

            connection.setAutoCommit(true);
            LOGGER.debug("AutoCommit enabled");

            initDatabase();
            LOGGER.warn("Database initialized");

        } catch (SQLException throwable) {
            LOGGER.warn(throwable);
        }
    }

    public BCryptPasswordEncoder getEncoder() {
        return encoder;
    }

    public static UsersRepository getInstance() {
        UsersRepository result = instance;
        if (result != null) {
            return result;
        }
        synchronized (UsersRepository.class) {
            if (UsersRepository.instance == null) {
                UsersRepository.instance = new UsersRepository();
            }
            return instance;
        }
    }

    private void initDatabase() {
        ScriptRunner runner = new ScriptRunner(connection);
        BufferedReader reader = new BufferedReader(new InputStreamReader(Objects.requireNonNull(StudentRepository.class.getClassLoader().getResourceAsStream("database/initUsers.sql"))));
        runner.setEscapeProcessing(false);
        runner.runScript(reader);

    }

    private String type(User user) {
        if(user instanceof Student)
            return "STUDENT";
        else if(user instanceof Coordinator)
            return "COORDINATOR";
        else if(user instanceof Teacher)
            return "TEACHER";
        return "USER";
    }

    public User insert(User user) throws AlreadyExistsException {
        // We need to specify the error, username and cpf need to be unique
        try (PreparedStatement insertStatement = Repository.connection.prepareStatement("INSERT INTO Users (username, password, name, cpf, address, birthday, type) VALUES (?1, ?2, ?3, ?4, ?5, ?6, ?7)")) {
            insertStatement.setString(1, user.getUsername());
            insertStatement.setString(2, encoder.encode(user.getPassword()));
            insertStatement.setString(3, user.getName());
            insertStatement.setString(4, user.getCpf());
            insertStatement.setString(5, user.getAddress());
            insertStatement.setString(6, user.getBirthday());
            insertStatement.setString(7, this.type(user));

            insertStatement.executeUpdate();

            LOGGER.debug(String.format("User with username %s was inserted to the database", user.getUsername()));
            long id = insertStatement.getGeneratedKeys().getLong(1);
            user.setId(id);

            insertStatement.close();
            return user;

        } catch (SQLException throwable) {
            LOGGER.warn(String.format("User with name '%s' could not be inserted to the database", user.getUsername()), throwable);

            String msg = throwable.getMessage();

            if(msg.contains("cpf")) {
                throw new AlreadyExistsException("O CPF ja esta cadastrado", throwable);
            } else {
                throw new AlreadyExistsException("O Username ja esta cadastrado", throwable);
            }
        }
    }


    // TODO: It will expand to other tables!!!!! PAY ATTENTION IN THAT
    public boolean deleteByCpf(String cpf) {
        try {
            PreparedStatement findStatement = connection.prepareStatement("SELECT * FROM Users WHERE cpf=?1");
            findStatement.setString(1, cpf);
            ResultSet findResultSet = findStatement.executeQuery();
            findResultSet.getString(1);

            PreparedStatement deleteStatement = connection.prepareStatement("DELETE FROM Users WHERE cpf=?1");
            deleteStatement.setString(1, cpf);

            deleteStatement.executeUpdate();
            LOGGER.debug(String.format("User with cpf %s was deleted from the database", cpf));
        } catch (SQLException throwable) {
            LOGGER.warn(String.format("User with cpf '%s' could not be found and deleted from the database", cpf), throwable);
            return false;
        }
        return true;
    }



    // TODO: It will expand to other tables!!!!! PAY ATTENTION IN THAT
    public boolean deleteByUsername(String username) {
        try {
            PreparedStatement findStatement = connection.prepareStatement("SELECT * FROM Users WHERE username=?1");
            findStatement.setString(1, username);
            ResultSet findResultSet = findStatement.executeQuery();
            findResultSet.getLong(1);

            PreparedStatement deleteStatement = connection.prepareStatement("DELETE FROM Users WHERE username=?1");
            deleteStatement.setString(1, username);

            deleteStatement.executeUpdate();
            LOGGER.debug(String.format("User with username '%s' was deleted from the database", username));
        } catch (SQLException throwable) {
            LOGGER.warn(String.format("User with username '%s' could not be found and deleted from the database", username), throwable);
            return false;
        }
        return true;
    }

    public Optional<User> findByUsername(String findName) {
        try {
            PreparedStatement findStatement = connection.prepareStatement("SELECT * FROM Users WHERE username=?1");
            findStatement.setString(1, findName);

            ResultSet findResultSet = findStatement.executeQuery();

            long id = findResultSet.getLong(1);
            String username = findResultSet.getString(2);
            String password = findResultSet.getString(3);
            String name = findResultSet.getString(4);
            String cpf = findResultSet.getString(5);
            String address = findResultSet.getString(6);
            String birthday = findResultSet.getString(7);

            String type = findResultSet.getString(8);

            User user;

            if(type.equals("STUDENT")) {
                // TODO: We will need to change it when we change the student
                user = new Student(id, username, password, name, cpf, address, birthday, null, null, null, null);
                LOGGER.debug(String.format("Student with id '%d' and username '%s' was found with success", user.getId(), user.getUsername()));
            } else {
                user = new User(id, username, password, name, cpf, address, birthday);
            }
            return Optional.of(user);
        } catch (SQLException throwable) {
            LOGGER.warn(String.format("Student with username '%s' could not be found", findName), throwable);
            return Optional.empty();
        }
    }

    public Optional<User> findByCpf(String findCpf) {
        try {
            PreparedStatement findStatement = connection.prepareStatement("SELECT * FROM Users WHERE cpf=?1");
            findStatement.setString(1, findCpf);

            ResultSet findResultSet = findStatement.executeQuery();

            long id = findResultSet.getLong(1);
            String username = findResultSet.getString(2);
            String password = findResultSet.getString(3);
            String name = findResultSet.getString(4);
            String cpf = findResultSet.getString(5);
            String address = findResultSet.getString(6);
            String birthday = findResultSet.getString(7);

            String type = findResultSet.getString(8);

            User user;

            if(type.equals("STUDENT")) {
                // TODO: We will need to change it when we change the student
                user = new Student(id, username, password, name, cpf, address, birthday, null, null, null, null);
                LOGGER.debug(String.format("Student with id '%d' and username '%s' was found with success", user.getId(), user.getUsername()));
            } else {
                user = new User(id, username, password, name, cpf, address, birthday);
            }
            return Optional.of(user);
        } catch (SQLException throwable) {
            LOGGER.warn(String.format("Student with username '%s' could not be found", findCpf), throwable);
            return Optional.empty();
        }
    }

    public boolean update(User user) {
        try {
            PreparedStatement findStatement = connection.prepareStatement("SELECT * FROM Users WHERE id=?1");
            findStatement.setLong(1, user.getId());


            ResultSet findResultSet = findStatement.executeQuery();
            findResultSet.getLong(1);

            PreparedStatement updateStatement = connection.prepareStatement("UPDATE Users SET username = ?1, password = ?2, name = ?3, cpf = ?4, address = ?5, birthday = ?6, type = ?7 WHERE id = ?8;");
            updateStatement.setString(1, user.getUsername());
            updateStatement.setString(2, user.getPassword());
            updateStatement.setString(3, user.getName());
            updateStatement.setString(4, user.getCpf());
            updateStatement.setString(5, user.getAddress());
            updateStatement.setString(6, user.getBirthday());
            updateStatement.setString(7, this.type(user));
            updateStatement.setLong(8, user.getId());

            updateStatement.executeUpdate();
            LOGGER.debug(String.format("User with id '%d' and username '%s' was updated", user.getId(), user.getUsername()));
        } catch (SQLException throwable) {
            LOGGER.warn(String.format("Student with id '%d' and username '%s' could not be updated", user.getId(), user.getUsername()), throwable);
            return false;
        }
        return true;
    }

    public static void main(String[] args) {
        StudentRepository sr = StudentRepository.getInstance();
        UsersRepository ur = UsersRepository.getInstance();
        SubjectRepository subr = SubjectRepository.getInstance();
        LectureRepository lr = LectureRepository.getInstance();


        Optional<Subject> sub = subr.findSubjectByCode("DCC01");


        if(sub.isEmpty())
            return;
        Subject subject = sub.get();

        LOGGER.debug(subject);

        lr.insert(new Lecture("O Plano", "13", "10:00-12:00", "ToP10",
                subject, new Teacher(2, "Braida", "1234"), new ArrayList<>()));

    }
}