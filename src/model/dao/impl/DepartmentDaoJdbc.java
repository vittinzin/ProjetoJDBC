package model.dao.impl;

import db.DB;
import db.DbException;
import db.DbIntegrityException;
import model.dao.DepartmenteDao;
import model.entities.Department;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class DepartmentDaoJdbc implements DepartmenteDao {

    private Connection conn;

    public DepartmentDaoJdbc(Connection conn) {
        this.conn = conn;
    }

    @Override
    public void insert(Department dp) {
        ResultSet rs = null;
        PreparedStatement ps = null;
        try {
            ps = conn.prepareStatement("INSERT INTO department (name) VALUES (?)", Statement.RETURN_GENERATED_KEYS);

            ps.setString(1, dp.getName());

            int rows = ps.executeUpdate();

            if (rows > 0) {
                rs = ps.getGeneratedKeys();
                while (rs.next()) {
                    int id = rs.getInt(1);
                    dp.setId(id);
                }
            } else {
                throw new DbException("No rows affected!");

            }
        } catch (DbIntegrityException | SQLException e) {
            throw new DbIntegrityException(e.getMessage());
        } finally {
            DB.closeStatement(ps);
            DB.closeResultSet(rs);
        }

    }

    @Override
    public void update(Department dp) {
        PreparedStatement ps = null;
        try {
            ps = conn.prepareStatement("UPDATE department SET name = ? WHERE id = ?");

            ps.setString(1, dp.getName());
            ps.setInt(2, dp.getId());

            ps.executeUpdate();

        } catch (DbIntegrityException | SQLException e) {
            throw new DbIntegrityException(e.getMessage());
        } finally {
            DB.closeStatement(ps);

        }
    }

    @Override
    public void deleteById(Integer id) {
        PreparedStatement ps = null;
        try {
            ps = conn.prepareStatement("DELETE FROM department WHERE id = ?");

            ps.setInt(1, id);

            int rows = ps.executeUpdate();

            if (rows > 0) {
                System.out.println(STR."Sellers with id \{id} deleted!");
            } else {
                System.out.println("No sellers with this id found!");
            }

        } catch (DbException | SQLException e) {
            throw new DbException(e.getMessage());
        } finally {
            DB.closeStatement(ps);
        }
    }

    @Override
    public Department findById(Integer id) {
        ResultSet rs = null;
        PreparedStatement ps = null;
        try {
            ps = conn.prepareStatement(
                    "SELECT * FROM department WHERE id = ?"
            );

            ps.setInt(1, id);

            rs = ps.executeQuery();

            while (rs.next()) {
                Department dp = new Department(rs.getInt(1), rs.getString(2));
                return dp;
            }

            return null;

        } catch (DbException | SQLException e) {
            throw new DbException(e.getMessage());
        } finally {
            DB.closeStatement(ps);
        }
    }

    @Override
    public List<Department> findAll() {
        ResultSet rs = null;
        List<Department> list = new ArrayList<>();

        PreparedStatement ps = null;
        try {
            ps = conn.prepareStatement(
                    "SELECT * FROM department"
            );

            rs = ps.executeQuery();

            while (rs.next()) {
                Department dp = new Department(rs.getInt(1), rs.getString(2));
                list.add(dp);
            }

            return list;

        } catch (DbException | SQLException e) {
            throw new DbException(e.getMessage());
        } finally {
            DB.closeStatement(ps);
            DB.closeResultSet(rs);
        }
    }
}
