package bitlab.tech.finish.messenger.models;

import bitlab.tech.finish.messenger.models.group_p.Group;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Entity
@Table(name = "t_users")
@Getter
@Setter
@ToString
public class User extends BaseModel implements UserDetails  {


    @Column(name = "email", unique = true)
    private String email;

    @Column(name = "username", unique = true)
    private String username;

    @Column(name = "password")
    private String password;

    @Column(name = "first_name", length = 50)
    private String firstName;

    @Column(name = "last_name", length = 50)
    private String lastName;

    @Column(name = "job")
    private String job;

    @Column(name = "avatar" ,columnDefinition = "TEXT")
    private String avatar;

    @Column(name = "phone_number")
    private String phoneNumber;

    @Column(name = "last_online")
    private LocalDateTime lastOnlineDate;

    @Column(name = "bio" ,columnDefinition = "TEXT")
    private String bio;

    @Column(name = "is_banned", columnDefinition = "boolean default false")
    private boolean isBanned;






    //relations

    @ManyToMany(fetch = FetchType.EAGER)
    @JsonIgnore
    @JoinTable(
            name = "t_friends", // name of the table
            joinColumns = @JoinColumn(name = "user_id"), // column, joined with current user
            inverseJoinColumns = @JoinColumn(name = "friend_id") // column, joined with friend
    )
    private Set<User> relatedUsers = new HashSet<>();

    @ManyToMany(fetch = FetchType.EAGER, cascade = CascadeType.ALL)
    @JsonIgnore
    private Set<Permission> permissions = new HashSet<>();

    @OneToMany(mappedBy = "fromUser")
    @JsonIgnore
    private List<Chat> fromChats;

    @OneToMany(mappedBy = "toUser")
    @JsonIgnore
    private List<Chat> toChats;

    @OneToMany(mappedBy = "user", fetch = FetchType.EAGER, cascade = CascadeType.ALL)
    @JsonIgnore
    private List<Post> posts;


    @ManyToMany
    @JsonIgnore
    private Set<Group> groups;


    @OneToMany(mappedBy = "author")
    @JsonIgnore
    private List<Group> createdGroups;



    // UserDetails methods
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return this.permissions;
    }

    @Override
    public String getPassword() {
        return this.password;
    }

    @Override
    public String getUsername() {
        return this.username;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }

    // custom methods
    public String loadUserAvatar(){  // if avatar is null or empty, return default avatar
        if(avatar == null || avatar.isEmpty()){
            return "/defaults/default-user.png";
        }
        return avatar;
    }
    public String getFullName(){
        if (firstName != null && lastName != null)
             return firstName + " " + lastName;
        else if (firstName != null)
            return firstName;
        else if (lastName != null)
            return lastName;
        else
            return username;
    }



}