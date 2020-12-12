package cn.edu.xmu.otherinterface.bo;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * @author issyu 30320182200070
 * @date 2020/12/11 8:39
 */
public class ShopInfo implements Serializable {

    private Long id;
    private String name;
    private Byte state;
    private LocalDateTime gmtCreate;
    private LocalDateTime gmtModified;
    private String message;

    public ShopInfo(String message) {
        this.message = message;
    }

    public ShopInfo(Long id, String name, Byte state, LocalDateTime gmtCreate, LocalDateTime gmtModified, String message) {
        this.id = id;
        this.name = name;
        this.state = state;
        this.gmtCreate = gmtCreate;
        this.gmtModified = gmtModified;
        this.message = message;
    }

    public LocalDateTime getGmtCreate() {
        return gmtCreate;
    }

    public void setGmtCreate(LocalDateTime gmtCreate) {
        this.gmtCreate = gmtCreate;
    }

    public LocalDateTime getGmtModified() {
        return gmtModified;
    }

    public void setGmtModified(LocalDateTime gmtModified) {
        this.gmtModified = gmtModified;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Byte getState() {
        return state;
    }

    public void setState(Byte state) {
        this.state = state;
    }
}
