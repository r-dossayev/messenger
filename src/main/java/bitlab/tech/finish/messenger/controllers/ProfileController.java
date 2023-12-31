package bitlab.tech.finish.messenger.controllers;

import bitlab.tech.finish.messenger.models.Post;
import bitlab.tech.finish.messenger.models.User;
import bitlab.tech.finish.messenger.services.FileStorageService;
import bitlab.tech.finish.messenger.services.PostService;
import bitlab.tech.finish.messenger.services.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.NoHandlerFoundException;

import java.io.IOException;

@Controller
@RequestMapping(value = "/profile")
@RequiredArgsConstructor
public class ProfileController {

    @Value("${users.images.path}")
    private String userImagesPath;  // get path from application.properties

    @Value("${posts.images.path}")
    private String postsImagesPath;  // get path from application.properties

    private final UserService userService;
    private final PostService postService;
    private final FileStorageService fileStorageService;

    @PreAuthorize("isAuthenticated()") // this annotation checks if user is authenticated
    @GetMapping // this method will be called when user goes to /profile
    public String userProfile() {
        return "redirect:/profile/" + userService.getCurrentSessionUser().getUsername();
    }

    @PreAuthorize("isAuthenticated()")
    @PostMapping(value = "/update/user/data")
    public String updateUser(
            @RequestParam("avatarFile") MultipartFile avatarFile,
            @RequestParam("firstName") String firstName,
            @RequestParam("lastName") String lastName,
            @RequestParam("email") String email,
            @RequestParam("job") String job,
            @RequestParam("phoneNumber") String phone,
            @RequestParam("bio") String about) {
        try {
            User user = userService.getCurrentSessionUser();
            user.setFirstName(firstName);
            user.setLastName(lastName);
            user.setEmail(email);
            user.setJob(job);
            user.setPhoneNumber(phone);
            user.setBio(about);
            if (!avatarFile.isEmpty()) {
                String fileName = fileStorageService.saveFile(avatarFile, userImagesPath);
                user.setAvatar(fileName);
            }
            userService.saveUser(user);
            return "redirect:/profile/" + user.getUsername();
        } catch (IOException e) {
            System.out.println(e.getMessage());
            return "redirect:/error?error";
        }
    }

    @PreAuthorize("isAuthenticated()")
    @PostMapping(value = "/change/password")
    public String toUpdatePassword(
            @RequestParam(name = "old_password") String oldPassword,
            @RequestParam(name = "new_password") String newPassword,
            @RequestParam(name = "renew_password") String repeatNewPassword) {
        if (newPassword.equals(repeatNewPassword)) {
            User user = userService.updatePassword(newPassword, oldPassword);
            if (user != null) {
                return "redirect:/profile/" + user.getUsername() + "?password-changed";
            } else {
                return "redirect:/profile/" + userService.getCurrentSessionUser().getUsername() + "?oldpassword-error";
            }
        } else {
            return "redirect:/profile/" + userService.getCurrentSessionUser().getUsername() + "?password-mismatch";
        }
    }

    @PreAuthorize("isAuthenticated()")
    @PostMapping(value = "/create/post")
    public String createPost(
            @RequestParam(name = "postText") String text,
            @RequestParam(name = "postImage") MultipartFile image) {
        try {
            Post post = new Post();
            post.setText(text);
            if (!image.isEmpty()) {
                String fileName = fileStorageService.saveFile(image, postsImagesPath); // save file to /resources/static/storage/posts
                post.setImage(fileName);
            }
            post.setUser(userService.getCurrentSessionUser());
            postService.save(post);
            return "redirect:/profile/" + userService.getCurrentSessionUser().getUsername() + "?post-created";
        } catch (IOException e) {
            return "redirect:/error?error";
        }
    }

    @GetMapping(value = "{username}") //url is /profile/{username}
    public String profilePage(@PathVariable String username, Model model) throws NoHandlerFoundException {
        User user = userService.getUserByUsername(username);
        if (user == null) { // if user is not found in database redirect to 404 page
            throw new NoHandlerFoundException("GET", "/profile/" + username, HttpHeaders.EMPTY);
        }
        User authUser = userService.getCurrentSessionUser();
        if (authUser != null && authUser.getId().equals(user.getId())) {

            model.addAttribute("user", user);
            return "profile"; // auth user to auth user profile
        } else if (authUser != null) {
            User authUser2 = userService.getUserByUsername(authUser.getUsername());
            boolean isFriend = authUser2.getRelatedUsers().stream()
                    .anyMatch(relatedUser -> relatedUser.getId().equals(user.getId())); // check if auth user is friend with user
            model.addAttribute("user", user);
            model.addAttribute("isFriend", !isFriend); // if auth user is friend with user then isFriend = false
            return "profiles/auth-u-to-u-prf"; // auth user to user profile
        }
        model.addAttribute("user", user);
        return "profiles/anon-u-to-u-prf"; // anon user to user profile


    }
    @PreAuthorize("isAuthenticated()")
    @PostMapping(value = "/delete/post")
    public String deletePost(@RequestParam(name = "postId") Long postId) {
        postService.deletePostById(postId);
        return "redirect:/profile/" + userService.getCurrentSessionUser().getUsername() + "?post-deleted";
    }

}
