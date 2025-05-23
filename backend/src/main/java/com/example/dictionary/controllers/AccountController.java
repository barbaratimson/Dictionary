package com.example.dictionary.controllers;

import com.example.dictionary.entities.Person;
import com.example.dictionary.security.JwtUtil;
import com.example.dictionary.services.DictionaryService;
import com.example.dictionary.services.PersonService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("")
public class AccountController {
    private final PersonService personService;
    private final JwtUtil jwtUtil;
    private final DictionaryService dictionaryService;

    @Autowired
    public AccountController(PersonService personService, JwtUtil jwtUtil, DictionaryService dictionaryService) {
        this.personService = personService;
        this.jwtUtil = jwtUtil;
        this.dictionaryService = dictionaryService;
    }

    @PatchMapping("/rename")
    public ResponseEntity<Map<String,String>> renameAccount(@RequestParam("new_name") String newName,
                                                    @RequestHeader("Authorization") String jwt){
        if(personService.checkIfExistsBy(newName)){
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
        else{
            personService.renameUser(newName, jwt);
            String newJwt = jwtUtil.rewriteUsernameInToken(newName, jwt.substring(7));
            return new ResponseEntity<>(Map.of("jwt", newJwt), HttpStatus.OK);
        }
    }

    @DeleteMapping("/delete")
    public void deleteAccount(@RequestHeader("Authorization") String jwt){
        Person person = personService.findByName(jwtUtil.validateTokenAndRetrieveClaim(jwt.substring(7)));
        dictionaryService.deleteDictionaries(person.getId());
        personService.deleteUser(person);
    }

    @PatchMapping("/change/pass")
    public ResponseEntity<HttpStatus> changePassword(@RequestBody Map<Object,String> newPass,
                                           @RequestHeader("Authorization") String jwt){
        personService.changePassword(newPass.get("password"), jwt);
        return ResponseEntity.ok(HttpStatus.OK);
    }
}
