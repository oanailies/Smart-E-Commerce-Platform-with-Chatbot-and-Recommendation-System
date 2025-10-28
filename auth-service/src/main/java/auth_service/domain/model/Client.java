package auth_service.domain.model;

import auth_service.application.dto.ClientDTO;
import jakarta.persistence.*;
import java.util.Date;

@Entity
@Table(name = "clients")
public class Client {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    private String phoneNumber;


    @OneToOne
    @JoinColumn(name = "user_account_id", nullable = false)
    private UserAccount userAccount;

    public Client() {
    }

    public Client(String name, String phoneNumber, UserAccount userAccount) {
        this.name = name;
        this.phoneNumber = phoneNumber;
        this.userAccount = userAccount;
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }



    public String getPhoneNumber() {
        return phoneNumber;
    }



    public UserAccount getUserAccount() {
        return userAccount;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setName(String name) {
        this.name = name;
    }



    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }



    public void setUserAccount(UserAccount userAccount) {
        this.userAccount = userAccount;
    }


}
