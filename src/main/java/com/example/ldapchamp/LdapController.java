package com.example.ldapchamp;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.ldap.core.AttributesMapper;
import org.springframework.ldap.core.LdapTemplate;
import org.springframework.ldap.query.SearchScope;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.naming.NamingException;
import javax.naming.directory.Attribute;
import javax.naming.directory.Attributes;
import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/ldap")
@Slf4j
public class LdapController {

    private LdapTemplate ldapTemplate;

    @Value("${ldapchamp.dn-key}")
    private String dnKey;

    public LdapController(LdapTemplate ldapTemplate) {
        this.ldapTemplate = ldapTemplate;
    }

    @GetMapping("/config")
    public Map<String, String> config() {
        Map<String, String> config = new LinkedHashMap<>();
        config.put("dnKey", dnKey);

        return config;
    }

    @GetMapping("/{base}")
    public Map<String, Object> searchByBaseAndFilter(@PathVariable String base, @RequestParam String filter) {
        if(base.equalsIgnoreCase("null")) {
            base = "";
        }
        Map<String, Object> searchCriteria = new LinkedHashMap<>();
        searchCriteria.put("base", base);
        searchCriteria.put("filter", filter);
        searchCriteria.put("start", Instant.now().toString());
        var result = search(base, filter);
        searchCriteria.put("end", Instant.now().toString());
        searchCriteria.put("size", result.size());
        searchCriteria.put("result", result);

        return searchCriteria;
    }

//    @GetMapping("/users/{userDn}")
//    public Map<String, Object> searchByUserDn(@PathVariable String userDn, @RequestParam(required = false) String groupBase) {
//
//    }

    @GetMapping("/by-dn/{entryDn}")
    public Map<String, Object> searchByUserDn(@PathVariable String entryDn, @RequestParam(required = false) String groupBase) {
        if(groupBase == null) {
            groupBase = "";
        }
        Map<String, Object> searchCriteria = new LinkedHashMap<>();
        searchCriteria.put(this.dnKey, entryDn);
        searchCriteria.put("groupBase", groupBase);
        searchCriteria.put("start", Instant.now().toString());

        var record = search("", this.dnKey + "=" + entryDn);

        var result = searchForUserGroups(groupBase, entryDn, this.dnKey, "member");
        searchCriteria.put("end", Instant.now().toString());
        searchCriteria.put("size", result.size());
        searchCriteria.put("record", record);
        searchCriteria.put("memberOf", result);
        return searchCriteria;
    }

    public List<String> unwrap(Attribute attribute) throws NamingException {
        List<String> result = new ArrayList<>();
        var attrs = attribute.getAll();
        while(attrs.hasMore()) {
            var next = attrs.next();
            result.add((String) next);
        }
        return result;
    }

    public List<String> searchForUserGroups(String groupBase, String userDn, String dnKey, String memberOfKey) {
        var results = this.ldapTemplate.search(groupBase, memberOfKey + "=" + userDn, 2, new String[]{this.dnKey}, new AttributesMapper<List<String>>() {
            @Override
            public List<String> mapFromAttributes(Attributes attributes) throws NamingException {
                List<String> groups = new ArrayList<>();
                var attrs = attributes.getAll();
                while(attrs.hasMore()) {
                    groups.add((String) attrs.next().get());
                }

                return groups;
            }
        });

        List<String> result = new ArrayList<>();
        results.forEach(list -> result.addAll(list));

        return result;
    }

    public List<Map<String, Object>> search(String base, String filter) {
        var result = this.ldapTemplate.search(base, filter, 2, new String[]{"*", this.dnKey, "manager"}, new AttributesMapper<Map<String, Object>>() {
            @Override
            public Map<String, Object> mapFromAttributes(Attributes attributes) throws NamingException {
                Map<String, Object> result = new LinkedHashMap<>();
                log.info("{}", attributes);
                var attrs = attributes.getAll();
                while(attrs.hasMore()) {
                    var next = attrs.next();
                    var id = next.getID();
                    // map all "dn" variants to dn
                    if(id.equalsIgnoreCase("entryDN")) {
                        id = "dn";
                    }
                    if(next.size() > 1 || id.equalsIgnoreCase("member")) {
                        result.put(id, unwrap(next));
                    } else {
                        result.put(id, next.get());
                    }
                }
                return result;
            }
        });

        return result;
    }
}
