package mate.academy.service.impl;

import java.util.Optional;
import mate.academy.exception.AuthenticationException;
import mate.academy.exception.DataProcessingException;
import mate.academy.exception.RegistrationException;
import mate.academy.model.User;
import mate.academy.service.AuthenticationService;
import mate.academy.service.UserService;
import mate.academy.util.PasswordUtil;

public class AuthenticationServiceImpl implements AuthenticationService {
    private final UserService userService;

    public AuthenticationServiceImpl(UserService userService) {
        this.userService = userService;
    }

    @Override
    public User login(String email, String password) {
        Optional<User> existingUser = userService.findByEmail(email);
        try {
            isUserEmpty(existingUser);
        } catch (AuthenticationException e) {
            throw new DataProcessingException("User with email: " + email + " not found", e);
        }

        String hash = PasswordUtil.hashPassword(password, existingUser.get().getSalt());

        try {
            isValidPassword(hash, existingUser.get().getPassword());
        } catch (AuthenticationException e) {
            throw new DataProcessingException("Incorrect password: " + password, e);
        }

        return existingUser.get();
    }

    @Override
    public User register(String email, String password) {
        Optional<User> existingUser = userService.findByEmail(email);
        try {
            isUserPresent(existingUser);
        } catch (RegistrationException e) {
            throw new DataProcessingException("User with email: " + email + " already exist", e);
        }

        byte[] salt = PasswordUtil.getSalt();

        String hashedPassword = PasswordUtil.hashPassword(password, salt);

        User user = new User();
        user.setEmail(email);
        user.setPassword(hashedPassword);
        user.setSalt(salt);

        return userService.add(user);
    }

    public void isUserPresent(Optional<User> userOptional) throws RegistrationException {
        if (userOptional.isPresent()) {
            throw new RegistrationException("User is exist");
        }
    }

    public void isUserEmpty(Optional<User> userOptional) throws AuthenticationException {
        if (userOptional.isEmpty()) {
            throw new AuthenticationException("User isn't exist");
        }
    }

    public void isValidPassword(String hash, String userPassword) throws AuthenticationException {
        if (!hash.equals(userPassword)) {
            throw new AuthenticationException("Incorrect password");
        }
    }
}
