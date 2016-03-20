/*
* To change this license header, choose License Headers in Project Properties.
* To change this template file, choose Tools | Templates
* and open the template in the editor.
*/
package engine;

import java.util.List;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import Utils.Converter;
import java.util.ArrayList;

/**
 *
 * @author root
 */
public class Query {
    private koneksi koneksi = new koneksi();
    private Converter converter = new Converter();
    
    
     private void dbg(Object data){
        System.out.println("Query : "+data);
    }
    
    /**
     *
     * @param field
     * @param table
     * @param condition
     */
    public ArrayList<Map<String, Object>> loadData(String[] listcolumn, String table, String condition, String[] data) throws SQLException{
        ArrayList result = new ArrayList();
        Connection con = koneksi.getcon();
        String column = "";
        for(int i = 0; i < listcolumn.length; i++){
            if(listcolumn.length < 2){
                column += listcolumn[i];
            }else{
                column += listcolumn[i]+", ";
            }
        }
        String sql = "SELECT "+ column +" FROM "+ table + " WHERE " + condition;
        dbg(sql);
        PreparedStatement prep = con.prepareStatement(sql);
        int i = 1;
        for(String dt : data){
            prep.setString(i, dt);
            i++;
        }
        ResultSet reset =  prep.executeQuery();
        while(reset.next()){
            for( String clmn : listcolumn){
                Map<String, Object> map = new HashMap<String, Object>();
                map.put(clmn, reset.getObject(clmn));
                result.add(map);
            }
        }
        return result;
    }
    /**
     *
     * @param field
     * @param table
     * @return
     */
    public ArrayList<Map<String, Object>> loadData(String[] listcolumn, String table) throws SQLException{
        ArrayList result = new ArrayList();
        Connection con = koneksi.getcon();
        Statement stat = con.createStatement();
        String column = "";
        for(int i = 0; i < listcolumn.length; i++){
            if(listcolumn.length < 2){
                column = listcolumn[i];
            }else{
                column += listcolumn[i]+", ";
            }
        }
        String sql = "SELECT "+ column + " FROM "+ table;
        ResultSet res = stat.executeQuery(sql);
        while(res.next()){
            for(String clmn : listcolumn){
                Map<String, Object> map = new HashMap<String, Object>();
                map.put(clmn, res.getObject(clmn));
                result.add(map);
            }
        }
        return result;
    }
    
    public boolean updateData(String table, Map<String, Object> listcolumn, String condition){
        boolean res = false;
        try{
            Connection con = new koneksi().getcon();
            Set<String> keys = listcolumn.keySet();
            String query = "";
            for(String key : keys){
                query += key+"=?, ";
            }
            String sql = "UPDATE "+ table + " SET "+ query;
            PreparedStatement prep = con.prepareStatement(sql);
            int index = 1;
            for(String key : keys){
                prep.setObject(index, key);
                index++;
            }
            prep.executeUpdate();
        }catch(Exception e){
            
        }
        
        return res;
    }
    
    public boolean deleteData(String table, String codition) throws SQLException{
        Connection con = koneksi.getcon();
        String sql = "DELETE FROM "+ table + " WHERE " + codition;
        Statement stat  =  con.createStatement();
        int res =  stat.executeUpdate(sql);
        if(res == 1){
            return true;
        }else{
            return false;
        }
    }
    
    public List getEnum(String table, String column) throws SQLException{
        List res = new ArrayList();
        Connection con = koneksi.getcon();
        String sql = "SHOW COLUMNS FROM `"+ table + "` WHERE Field = '"+column+"'";
        Statement stat = con.createStatement();
        ResultSet result = stat.executeQuery(sql);
        while(result.next()){
            res.add(result.getString("Type"));
        }
        return res;
    }
//    Function for insert data
    public void insertData(String table, Map<String, Object> data){
        try{
//            call connectino from class koneksi
            Connection con = koneksi.getcon();
//            get key from data Map
            Set<String> keys = data.keySet();
            String keyQuery = "";
            String valueQuery = "";
            String sql = "";
//            set query from key for build query.
            if (keys != null){
                keyQuery += "( ";
                int x = 0;
                for (String key : keys) {
                    if(x > 0){
                        keyQuery += ","+key+" ";
                    }else{
                        keyQuery += key+" ";
                    }
                    x++;
                }
                keyQuery += ")";
//                count how many value from data
                valueQuery += "( ";
                for (int i = 0; i < data.size(); i++) {
                    if(i > 0){
                     valueQuery += ",? ";   
                    }else{
                        valueQuery += "? ";
                    }
                }
                valueQuery += ")";
            }
//            generate and execute query
            if(keyQuery != "" && valueQuery != ""){
                sql = "INSERT INTO "+ table+ " " + keyQuery + " VALUES " + valueQuery;
                PreparedStatement prep = con.prepareStatement(sql);
                int index = 1;
                for (String key : keys) {
                    prep.setObject(index, data.get(key));
                    index++;
                }
                prep.executeUpdate();
            }
        }catch(Exception e){
            
        }
        
    }
    
    public String generateID(String pattern, String table, String column){
        String id = "";
        try {
            int length_id = 0;
            Connection con = koneksi.getcon();
            String sql_check_length = "SELECT character_maximum_length AS length FROM information_schema.columns WHERE table_name = ? AND column_name = ?";
            String sql_check_id = "SELECT " + column + " FROM " + table + " ORDER BY " + column + " DESC";
            PreparedStatement prep_length = con.prepareStatement(sql_check_length);
            PreparedStatement prep_check = con.prepareStatement(sql_check_id);
//            set parameter on query sql for check length
            prep_length.setObject(1, table);
            prep_length.setObject(2, column);
            
            ResultSet res_length = prep_length.executeQuery();
            ResultSet res_check = prep_check.executeQuery();
            
            if(res_length.next()){
                length_id = res_length.getInt("length");
            }
            if(res_check.next()){
                String before = res_check.getString(column);
                int before_id = Integer.parseInt(before.substring(pattern.length(), length_id));
                String after_id = String.valueOf(before_id + 1);
                id = pattern+converter.pad_zero(after_id, length_id);
            }else if(!res_check.isBeforeFirst()){
                id = pattern+converter.pad_zero("1", length_id);
            }
            
        } catch (Exception e) {
        }
        return id; 
    }
}


// Sample to use function

		Map<String, Object> dataStaff = new HashMap<String, Object>();
        dataStaff.put("id_staff", id);
        dataStaff.put("nama", nama);
        dataStaff.put("no_telp", no_telp);
        dataStaff.put("alamat", alamat);
        dataStaff.put("username", username);
        query.insertData("tb_staff", dataStaff);

// code for fun learn friend