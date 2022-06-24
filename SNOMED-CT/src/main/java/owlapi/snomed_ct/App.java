package owlapi.snomed_ct;

import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyManager;

public class App {
    public static void main(String[] args) {
        snomed_ct main = new snomed_ct();

        ClassLoader classloader = Thread.currentThread().getContextClassLoader();
        OWLOntologyManager manager1 = OWLManager.createOWLOntologyManager();
        OWLOntologyManager manager2 = OWLManager.createOWLOntologyManager();
        OWLOntologyManager manager3 = OWLManager.createOWLOntologyManager();
        OWLOntologyManager manager4 = OWLManager.createOWLOntologyManager();
        OWLOntologyManager manager5 = OWLManager.createOWLOntologyManager();
        OWLOntologyManager manager6 = OWLManager.createOWLOntologyManager();

        OWLOntology ontology1 = main.loadOntology(manager1,
                classloader.getResourceAsStream("ontology_201801.owl"));
        OWLOntology ontology2 = main.loadOntology(manager2,
                classloader.getResourceAsStream("ontology_201807.owl"));
        OWLOntology ontology3 = main.loadOntology(manager3,
                classloader.getResourceAsStream("ontology_201901.owl"));
        OWLOntology ontology4 = main.loadOntology(manager4,
                classloader.getResourceAsStream("ontology_201907.owl"));
        OWLOntology ontology5 = main.loadOntology(manager5,
                classloader.getResourceAsStream("ontology_202001.owl"));
        OWLOntology ontology6 = main.loadOntology(manager6,
                classloader.getResourceAsStream("ontology_202007.owl"));

        /* 这里的diff一起跑会error，可以两个为一组分批运行 */
        main.diff(ontology1, ontology2, "201801-201807-witnesses.owl");
        main.diff(ontology2, ontology1, "201807-201801-witnesses.owl");
        main.diff(ontology2, ontology3, "201807-201901-witnesses.owl");
        main.diff(ontology3, ontology2, "201901-201807-witnesses.owl");
        main.diff(ontology3, ontology4, "201901-201907-witnesses.owl");
        main.diff(ontology4, ontology3, "201907-201901-witnesses.owl");
        main.diff(ontology4, ontology5, "201907-202001-witnesses.owl");
        main.diff(ontology5, ontology4, "202001-201907-witnesses.owl");
        main.diff(ontology5, ontology6, "202001-202007-witnesses.owl");
        main.diff(ontology6, ontology5, "202007-202001-witnesses.owl");

        main.createSig(ontology1, ontology2, "sig12.txt");
        main.createSig(ontology2, ontology3, "sig23.txt");
        main.createSig(ontology3, ontology4, "sig34.txt");
        main.createSig(ontology4, ontology5, "sig45.txt");
        main.createSig(ontology5, ontology6, "sig56.txt");


        main.sig_view(ontology1, "sig12.txt", "sig12_onto1.owl");
        main.sig_view(ontology2, "sig12.txt", "sig12_onto2.owl");


        OWLOntologyManager manager12_1 = OWLManager.createOWLOntologyManager();

        OWLOntology sig12_onto1 = main.loadOntology(manager12_1,
                classloader.getResourceAsStream("sig12_onto1.owl"));

        /* 因为部分数据会发生OutOfMemoryError, 因此只计算了201807-201801-view-witnesses.owl */
        main.diff(ontology2, sig12_onto1, "201807-201801-view-witnesses.owl");
    }
}
