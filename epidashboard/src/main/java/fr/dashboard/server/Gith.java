package fr.dashboard.server;

import org.eclipse.egit.github.core.Repository;
import org.eclipse.egit.github.core.service.RepositoryService;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.text.MessageFormat;
import java.util.ArrayList;

@RestController
public class Gith {

    /**
     * Liste les repositories Github recherchés
     * @param search Nom du repository recherché
     * @return Liste des repositories correspondant à la recherche
     */
    @RequestMapping({"/githubRepoSearch"})
    public static ArrayList<ArrayList<String>> githubRepoSearch(@RequestParam("rechearch") String search) {
        ArrayList<ArrayList<String>> results = new ArrayList<ArrayList<String>>();
        ArrayList<String> tmp = new ArrayList<String>();
        RepositoryService service = new RepositoryService();
        try {
            for (Repository repo : service.getRepositories(search)) {
                tmp = new ArrayList<String>();
                tmp.add(repo.getName());
                tmp.add(repo.getUrl());
                tmp.add(repo.getHomepage());
                tmp.add(repo.getDescription());
                tmp.add(repo.getOwner().getName());
                tmp.add(repo.getOwner().getUrl());
            }
            results.add(tmp);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return results;
    }
}