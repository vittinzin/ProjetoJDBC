package model.dao.impl;

import db.DB;
import db.DbException;
import db.DbIntegrityException;
import model.dao.SellerDao;
import model.entities.Department;
import model.entities.Seller;

import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class SellerDaoJdbc implements SellerDao {

    private Connection conn;

    public SellerDaoJdbc(Connection conn) {
        this.conn = conn;
    }

    @Override
    public void insert(Seller sell) {
        PreparedStatement st = null;
        try {
            st = conn.prepareStatement(
                    "INSERT INTO seller (Name, Email, BirthDate, BaseSalary," +
                            "DepartmentId) VALUES (?,?,?,?,?)", Statement.RETURN_GENERATED_KEYS
            );

            st.setString(1, sell.getName());
            st.setString(2, sell.getEmail());
            st.setDate(3, new java.sql.Date(sell.getBirthDate().getTime()));
            st.setDouble(4, sell.getBaseSalary());
            st.setInt(5, sell.getDepartment().getId());

            int rows = st.executeUpdate();
            if (rows > 0) {
                ResultSet rs = st.getGeneratedKeys();
                if (rs.next()) {
                    int id = rs.getInt(1);
                    sell.setId(id);
                }
                DB.closeResultSet(rs);
            } else {
                throw new DbException("No rows affected!");
            }

        } catch (DbIntegrityException | SQLException e) {
            throw new DbIntegrityException(e.getMessage());
        } finally {
            DB.closeStatement(st);
        }
    }

    @Override
    public void update(Seller sell) {
        PreparedStatement ps = null;
        try {
            ps = conn.prepareStatement(
                    "UPDATE seller SET name = ?, Email = ?, BirthDate = ?, BaseSalary = ?, " +
                            "DepartmentId = ? WHERE Id = ?"
            );

            ps.setString(1, sell.getName());
            ps.setString(2, sell.getEmail());
            ps.setDate(3, new java.sql.Date(sell.getBirthDate().getTime()));
            ps.setDouble(4, sell.getBaseSalary());
            ps.setInt(5, sell.getDepartment().getId());
            ps.setInt(6, sell.getId());

            ps.executeUpdate();

        } catch (SQLException e) {
            throw new DbException(e.getMessage());
        } finally {
            DB.closeStatement(ps);
        }
    }

    @Override
    public void deleteById(Integer id) {
        PreparedStatement ps = null;
        try {
            ps = conn.prepareStatement(
                    "DELETE FROM seller WHERE id = ?"
            );

            ps.setInt(1, id);

            int rows = ps.executeUpdate();

            if (rows > 0) {
                System.out.println(STR."Sellers with id \{id} deleted!");
            } else {
                System.out.println("No sellers with this id found!");
            }

        } catch (SQLException e) {
            throw new DbException(e.getMessage());
        } finally {
            DB.closeStatement(ps);
        }
    }

    @Override
    public Seller findById(Integer id) {
        ResultSet rs = null;
        PreparedStatement ps = null;
        try {
            ps = conn.prepareStatement(
                    "SELECT seller.*,department.Name as DepName" +
                            " FROM seller INNER JOIN department" +
                            " ON seller.DepartmentId = department.Id" +
                            " WHERE seller.Id = ?"
            );

            ps.setInt(1, id);
            rs = ps.executeQuery();

            if (rs.next()) {
                Department dp = instantiateDepartment(rs);
                Seller seller = instantiateSeller(rs, dp);
                return seller;
            }
            return null;

        } catch (DbIntegrityException | SQLException e) {
            throw new DbIntegrityException(e.getMessage());
        } finally {
            DB.closeStatement(ps);
            DB.closeResultSet(rs);
        }
    }

    @Override
    public List<Seller> findAll() {

        List<Seller> sellers;
        ResultSet rs = null;
        PreparedStatement ps = null;
        try {
            ps = conn.prepareStatement(
                    "SELECT seller.*,department.Name as DepName "
                            + " FROM seller INNER JOIN department "
                            + " ON seller.DepartmentId = department.Id "
                            + " ORDER BY Name"
            );

            rs = ps.executeQuery();

            sellers = new ArrayList<>();
            Map<Integer, Department> departments = new HashMap<>();

            while (rs.next()) {

                Department dep = departments.get(rs.getInt("DepartmentId"));

                if (dep == null) {
                    dep = instantiateDepartment(rs);
                    departments.put(rs.getInt("DepartmentId"), dep);
                }

                Seller seller = instantiateSeller(rs, dep);
                sellers.add(seller);
            }


        } catch (SQLException e) {
            throw new DbException(e.getMessage());
        } finally {
            DB.closeStatement(ps);
            DB.closeResultSet(rs);
        }
        return sellers;
    }

    @Override
    public List<Seller> findByDepartment(Department department) {
        List<Seller> sellers = new ArrayList<>();
        ResultSet rs = null;
        PreparedStatement ps = null;
        try {
            ps = conn.prepareStatement(
                    "SELECT seller.*,department.Name as DepName "
                            + "FROM seller INNER JOIN department "
                            + "ON seller.DepartmentId = department.Id "
                            + "WHERE DepartmentId = ? "
                            + "ORDER BY Name"
            );

            ps.setInt(1, department.getId());
            rs = ps.executeQuery();

            sellers = new ArrayList<>();
            Map<Integer, Department> departments = new HashMap<>();

            while (rs.next()) {

                Department dep = departments.get(rs.getInt("DepartmentId"));

                if (dep == null) {
                    dep = instantiateDepartment(rs);
                    departments.put(rs.getInt("DepartmentId"), dep);
                }

                Seller seller = instantiateSeller(rs, dep);
                sellers.add(seller);
            }

            return sellers;

        } catch (SQLException e) {
            throw new DbException(e.getMessage());
        } finally {
            DB.closeStatement(ps);
            DB.closeResultSet(rs);
        }
    }

    private Seller instantiateSeller (ResultSet rs, Department department) throws SQLException {
        Seller seller = new Seller(rs.getInt("Id"), rs.getString("Name"), rs.getString("Email"),
                rs.getDate("BirthDate"), rs.getDouble("BaseSalary"), department);

        return seller;
    }

    private Department instantiateDepartment (ResultSet rs) throws SQLException {
        Department department = new Department(rs.getInt("Id"), rs.getString("Name"));

        return department;
    }
}
