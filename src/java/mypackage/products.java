package mypackage;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
import credentials.Credentials;
import java.sql.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.json.Json;
import javax.json.JsonObject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import org.json.simple.*;

/**
 *
 * @author Swar
 */
@Path("/products")
public class products {

    @GET
    @Produces("application/json")
    public String getAll(@QueryParam("id") String id) {
        if (id == null) {
            return (getResults("SELECT * FROM PRODUCT"));
        } else {
            return (getResults("SELECT * FROM PRODUCT WHERE productId = ?", id));
        }
    }

    private String getResults(String query, String... params) {
        JSONArray jArray = new JSONArray();
        StringBuilder sb = new StringBuilder();
        Boolean isSingle = false;
        try (Connection cn = Credentials.getConnection()) {
            PreparedStatement pstmt = cn.prepareStatement(query);
            for (int i = 1; i <= params.length; i++) {
                pstmt.setString(i, params[i - 1]);
                isSingle = true;
            }
            ResultSet rs = pstmt.executeQuery();
            if (isSingle == false) {
                while (rs.next()) {
                    JSONObject json = new JSONObject();
                    json.put("productId", rs.getInt("productId"));
                    json.put("name", rs.getString("name"));
                    json.put("description", rs.getString("description"));
                    json.put("quantity", rs.getInt("quantity"));
                    jArray.add(json);
                }
            } else {
                while (rs.next()) {
                    JsonObject jsonObj = Json.createObjectBuilder()
                            .add("productId", rs.getInt("productId"))
                            .add("name", rs.getString("name"))
                            .add("description", rs.getString("description"))
                            .add("quantity", rs.getInt("quantity"))
                            .build();
                    return jsonObj.toString();
                }
            }
        } catch (SQLException ex) {
            Logger.getLogger(products.class.getName()).log(Level.SEVERE, null, ex);
        }
        return jArray.toJSONString();
    }


}
