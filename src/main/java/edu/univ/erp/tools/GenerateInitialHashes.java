package edu.univ.erp.tools;

import edu.univ.erp.auth.PasswordHasher;

public class GenerateInitialHashes {

    public static void main(String[] args) {
        System.out.println("admin1 / admin123 => " + PasswordHasher.hash("admin123"));
        System.out.println("inst1  / inst123  => " + PasswordHasher.hash("inst123"));
        System.out.println("inst2   / inst234   => " + PasswordHasher.hash("inst234"));
        System.out.println("stu1   / stu123   => " + PasswordHasher.hash("stu123"));
        System.out.println("stu2   / stu234   => " + PasswordHasher.hash("stu234"));

    }
}

