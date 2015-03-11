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
import javax.json.JsonArrayBuilder;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
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
        int status = doUpdate("INSERT INTO PRODUCT (productId, name, description, quantity) VALUES (?, ?, ?, ?)", id, name, description, qty);
        if (status == 0) {
            return Response.status(500).build();
        } else {
            return Response.ok("http://localhost:8080/CPD-4414-Assignment-4/products/"
                    + id,
                    MediaType.TEXT_HTML).build();
        }
    }

    @PUT
    @Path("{id}")
    @Consumes("application/json")
    public Response putData(String str, @PathParam("id") int id) {
        JsonObject json = Json.createReader(new StringReader(str)).readObject();
        String id1 = String.valueOf(id);
        String name = json.getString("name");
        String description = json.getString("description");
        String qty = String.valueOf(json.getInt("quantity"));
        int status = doUpdate("UPDATE PRODUCT SET productId= ?, name = ?, description = ?, quantity = ? WHERE productId = ?", id1, name, description, qty, id1);
        if (status == 0) {
            return Response.status(500).build();
        } else {
            return Response.ok("http://localhost:8080/CPD-4414-Assignment-4/products/"
                    + id,
                    MediaType.TEXT_HTML).build();
        }
    }

    @DELETE
    @Path("{id}")
    @Consumes("application/json")
    public Response delete(@PathParam("id") String id) {
        if (doUpdate("DELETE FROM PRODUCT WHERE productId = ?", id) == 0) {
            return Response.status(500).build();
        } else {
            return Response.ok().build();
        }
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
        StringBuilder sb = new StringBuilder();
        Boolean isSingle = false;
        JsonArrayBuilder jArray = Json.createArrayBuilder();
        try (Connection cn = Credentials.getConnection()) {
            PreparedStatement pstmt = cn.prepareStatement(query);
            for (int i = 1; i <= params.length; i++) {
                pstmt.setString(i, params[i - 1]);
                isSingle = true;
            }
            ResultSet rs = pstmt.executeQuery();
            if (isSingle == false) {
                while (rs.next()) {
                    JsonObjectBuilder productBuilder = Json.createObjectBuilder();
                    productBuilder.add("productId", rs.getInt("productId"))
                            .add("name", rs.getString("name"))
                            .add("description", rs.getString("description"))
                            .add("quantity", rs.getInt("quantity"));
                    jArray.add(productBuilder);
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
        return jArray.build().toString();
    }

}
