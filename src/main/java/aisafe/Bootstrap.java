package aisafe;

import aisafe.aircrafts.domain.AircraftModel;
import aisafe.aircrafts.domain.AircraftModelRepository;
import aisafe.security.domain.Role;
import aisafe.security.domain.User;
import aisafe.security.domain.UserRepository;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
//@Profile("dev")
public class Bootstrap implements ApplicationRunner {
    private final UserRepository userRepository;
    private final AircraftModelRepository aircraftModelRepository;
    private final PasswordEncoder passwordEncoder;

    public Bootstrap(UserRepository userRepository, AircraftModelRepository aircraftModelRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.aircraftModelRepository = aircraftModelRepository;
        this.passwordEncoder = passwordEncoder;
    }


    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        if (aircraftModelRepository.count() == 0){
           aircraftModelRepository.save(new AircraftModel("Boeing 737 MAX", "Boeing", 25941.0, 6570.0, 839.0, "images/b737max.png"));
        }
        if (userRepository.count() == 0){
            userRepository.save(new User("admin", passwordEncoder.encode("admin123"), Role.ADMIN));
        }
    }
}
