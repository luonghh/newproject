/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package nhom11ltm;

/**
 *
 * @author HL
 */
public class Users {
    private String username, password;
    private float  point ;

    public Users() {
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    public float getPoint() {
        return point;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public void setPoint(float point) {
        this.point = point;
    }
    
}
