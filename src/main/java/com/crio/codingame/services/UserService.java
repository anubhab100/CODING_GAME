package com.crio.codingame.services;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import com.crio.codingame.dtos.UserRegistrationDto;
import com.crio.codingame.entities.Contest;
import com.crio.codingame.entities.ContestStatus;
import com.crio.codingame.entities.RegisterationStatus;
import com.crio.codingame.entities.ScoreOrder;
import com.crio.codingame.entities.User;
import com.crio.codingame.exceptions.ContestNotFoundException;
import com.crio.codingame.exceptions.InvalidOperationException;
import com.crio.codingame.exceptions.UserNotFoundException;
import com.crio.codingame.repositories.IContestRepository;
import com.crio.codingame.repositories.IUserRepository;

public class UserService implements IUserService {

    private final IUserRepository userRepository;
    private final IContestRepository contestRepository;
    private int nextUserId = 1; // Auto-incrementing user ID


    public UserService(IUserRepository userRepository, IContestRepository contestRepository) {
        this.userRepository = userRepository;
        this.contestRepository = contestRepository;
    }
    // TODO: CRIO_TASK_MODULE_SERVICES
    // Create and store User into the repository.
    @Override
    public User create(String name) {
        User newUser = new User(Integer.toString(nextUserId), name, 1500); // Assigning auto-incremented ID and default score of 1500
        nextUserId++; // Incrementing the user ID for the next user
        return userRepository.save(newUser);
    }

    // TODO: CRIO_TASK_MODULE_SERVICES
    // Get All Users in Ascending Order w.r.t scores if ScoreOrder ASC.
    // Or
    // Get All Users in Descending Order w.r.t scores if ScoreOrder DESC.


    @Override
    public List<User> getAllUserScoreOrderWise(ScoreOrder scoreOrder){
        List<User> users = userRepository.findAll(); // Get all users from the repository

        // Sort users based on score order
        if (scoreOrder == ScoreOrder.ASC) {
            Collections.sort(users, Comparator.comparing(User::getScore));
        } else {
            Collections.sort(users, Comparator.comparing(User::getScore).reversed());
        }

        return users;
    }

    @Override
    public UserRegistrationDto attendContest(String contestId, String userName) throws ContestNotFoundException, UserNotFoundException, InvalidOperationException {
        Contest contest = contestRepository.findById(contestId).orElseThrow(() -> new ContestNotFoundException("Cannot Attend Contest. Contest for given id:"+contestId+" not found!"));
        User user = userRepository.findByName(userName).orElseThrow(() -> new UserNotFoundException("Cannot Attend Contest. User for given name:"+ userName+" not found!"));
        if(contest.getContestStatus().equals(ContestStatus.IN_PROGRESS)){
            throw new InvalidOperationException("Cannot Attend Contest. Contest for given id:"+contestId+" is in progress!");
        }
        if(contest.getContestStatus().equals(ContestStatus.ENDED)){
            throw new InvalidOperationException("Cannot Attend Contest. Contest for given id:"+contestId+" is ended!");
        }
        if(user.checkIfContestExists(contest)){
            throw new InvalidOperationException("Cannot Attend Contest. Contest for given id:"+contestId+" is already registered!");
        }
        user.addContest(contest);
        userRepository.save(user);
        return new UserRegistrationDto(contest.getName(), user.getName(),RegisterationStatus.REGISTERED);
    }

    // TODO: CRIO_TASK_MODULE_SERVICES
    // Withdraw the user from the contest
    // Hint :- Refer Unit Testcases withdrawContest method

    @Override
    public UserRegistrationDto withdrawContest(String contestId, String userName) throws ContestNotFoundException, UserNotFoundException, InvalidOperationException {
     // Find the contest
     Contest contest = contestRepository.findById(contestId)
     .orElseThrow(() -> new ContestNotFoundException("Contest not found with ID: " + contestId));

// Find the user
User user = userRepository.findByName(userName)
     .orElseThrow(() -> new UserNotFoundException("User not found with name: " + userName));

// Check if the user is registered for the contest
if (!user.checkIfContestExists(contest)) {
 throw new InvalidOperationException("User " + userName + " is not registered for contest " + contestId);
}

// Withdraw the user from the contest
user.deleteContest(contest);
userRepository.save(user);

return new UserRegistrationDto(contest.getName(), user.getName(), RegisterationStatus.NOT_REGISTERED);
}
    
    
}
