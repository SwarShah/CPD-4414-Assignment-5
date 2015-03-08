package mypackage;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
import credentials.Credentials;
import java.io.StringReader;
import java.sql.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.json.Json;
import javax.json.JsonObject;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import org.json.simple.*;

/**
 *
 * @author Swar
 */
@Path("products")
public class products {

    @GET
    @Produces("application/json")
    public String getAll() {
        return (getResults("SELECT * FROM PRODUCT"));
        //    return (getResults("SELECT * FROM PRODUCT WHERE productId = ?", id));
    }

    @GET
    @Path("{id}")
    @Produces("application/json")
    public String get(@PathParam("id") int id) {
        return (getResults("SELECT * FROM PRODUCT WHERE productId = ?", String.valueOf(id)));
    }

    @POST
    @Consumes("application/json")
    public Response post(String str) {
        JsonObject json = Json.createReader(new StringReader(str)).readObject();
        String id = String.valueOf(json.getInt("productId"));
        String name = json.getString("name");
        String description = json.getString("description");
        String qty = String.valueOf(json.getInt("quantity"));
        doUpdate("INSERT INTO PRODUCT (productId, name, description, quantity) VALUES (?, ?, ?, ?)", id, name, description, qty);
        return Response.ok("http://localhost:8080/CPD-4414-Assignment-4/products/"+
                        id,
                        MediaType.TEXT_HTML).build(); 
    }
    
    @PUT
    @Path("{id}")
    @Consumes("application/json")
    public void putData(String str, @PathParam("id") int id){
        JsonObject json = Json.createReader(new StringReader(str)).readObject();
        String id1 = String.valueOf(id);
        String name = json.getString("name");
        String description = json.getString("description");
        String qty = String.valueOf(json.getInt("quantity"));
        doUpdate("UPDATE PRODUCT SET productId= ?, name = ?, description = ?, quantity = ? WHERE productId = ?", id1, name, description, qty, id1);
    }
    
    @DELETE
    @Path("{id}")
    @Consumes("application/json")
    public void delete(@PathParam("id") String id) {
        doUpdate("DELETE FROM PRODUCT WHERE productId = ?", id);
    }
    
    public int doUpdate(String query, String... params) {
        int changes = 0;
        try (Connection cn = Credentials.getConnection()) {
            PreparedStatement pstmt = cn.prepareStatement(query);
            for (int i = 1; i <= params.length; i++) {
                pstmt.setString(i, params[i - 1]);
            }
            changes = pstmt.executeUpdate();
        } catch (SQLException ex) {
            Logger.getLogger(products.class.getName()).log(Level.SEVERE, null, ex);
        }
        return changes;
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
