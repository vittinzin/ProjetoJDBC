package model.dao;

import model.entities.Department;
import model.entities.Seller;

import java.util.List;

public interface DepartmenteDao {

    void insert (Department dp);
    void update (Department dp);
    void deleteById (Integer id);
    Department findById (Integer id);
    List<Department> findAll();

}
