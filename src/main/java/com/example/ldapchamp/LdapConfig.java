package com.example.ldapchamp;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.ldap.core.LdapTemplate;
import org.springframework.ldap.core.support.LdapContextSource;

@Configuration
public class LdapConfig {

    @Value("${ldapchamp.ignore-partial-result-exception}")
    private boolean ignorePartialResultException = true;

    @Value("${ldapchamp.url}")
    private String url;
    @Value("${ldapchamp.base}")
    private String base;
    @Value("${ldapchamp.user-dn}")
    private String userDn;
    @Value("${ldapchamp.password}")
    private String password;

    @Bean
    public LdapContextSource contextSource() {
        LdapContextSource contextSource = new LdapContextSource();
        contextSource.setUrl(url);
        contextSource.setBase(base);
        contextSource.setUserDn(userDn);
        contextSource.setPassword(password);

        return contextSource;
    }

    @Bean
    public LdapTemplate ldapTemplate() {
        var template = new LdapTemplate(contextSource());
        template.setIgnorePartialResultException(ignorePartialResultException);
        return template;
    }
}
