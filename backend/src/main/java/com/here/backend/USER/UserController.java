// package com.here.backend.USER;



// import org.springframework.beans.factory.annotation.Autowired;
// import org.springframework.http.HttpStatus;
// import org.springframework.http.ResponseEntity;
// import org.springframework.web.bind.annotation.*;

// import java.util.List;
// import java.util.Optional;

// @RestController
// @RequestMapping("/users")
// public class UserController {

//     @Autowired
//     private UserRepository userRepository;

//     @GetMapping
//     public List<User> getAllUsers() {
//         return userRepository.findAll();
//     }

//     @GetMapping("/{id}")
//     public ResponseEntity<User> getUserById(@PathVariable Long id) {
//         Optional<User> user = userRepository.findById(id);
//         return user.map(value -> ResponseEntity.ok(value))
//                    .orElse(ResponseEntity.notFound().build());
//     }

//     @PostMapping
//     public ResponseEntity<User> createUser(@RequestBody User user) {
//         if (((Optional<User>) userRepository.findByEmail(user.getEmail())).isPresent()) {
//             return ResponseEntity.status(HttpStatus.CONFLICT).build();
//         }
//         User savedUser = userRepository.save(user);
//         return ResponseEntity.status(HttpStatus.CREATED).body(savedUser);
//     }

//     @PutMapping("/{id}")
//     public ResponseEntity<User> updateUser(@PathVariable Long id, @RequestBody User updatedUser) {
//         return userRepository.findById(id).map(user -> {
//             user.setName(updatedUser.getName());
//             user.setEmail(updatedUser.getEmail());
//             userRepository.save(user);
//             return ResponseEntity.ok(user);
//         }).orElse(ResponseEntity.notFound().build());
//     }

//     @DeleteMapping("/{id}")
//     public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
//         if (userRepository.existsById(id)) {
//             userRepository.deleteById(id);
//             return ResponseEntity.noContent().build();
//         }
//         return ResponseEntity.notFound().build();
//     }
// }
