package owlapi;

import com.clarkparsia.pellet.owlapiv3.PelletReasonerFactory;
import junit.framework.TestCase;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.*;
import org.semanticweb.owlapi.model.parameters.Imports;
import org.semanticweb.owlapi.reasoner.OWLReasoner;
import org.semanticweb.owlapi.reasoner.OWLReasonerFactory;
import owlapi.snomed_ct.snomed_ct;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class snomed_ctTest extends TestCase {

    public void testDiff() {
        snomed_ct main = new snomed_ct();

        ClassLoader classloader = Thread.currentThread().getContextClassLoader();
        OWLOntologyManager manager1 = OWLManager.createOWLOntologyManager();
        OWLOntologyManager manager2 = OWLManager.createOWLOntologyManager();
        OWLOntologyManager manager_w = OWLManager.createOWLOntologyManager();

        OWLOntology ontology1 = main.loadOntology(manager1,
                classloader.getResourceAsStream("ontology_201801.owl"));
        OWLOntology ontology2 = main.loadOntology(manager2,
                classloader.getResourceAsStream("ontology_201807.owl"));
        assertNotNull(ontology1);
        assertNotNull(ontology2);

        OWLOntology witness = null;
        try {
            witness = manager_w.createOntology();
        } catch (OWLOntologyCreationException e) {
            e.printStackTrace();
        }

        OWLReasonerFactory reasonerFactory1 = new PelletReasonerFactory();
        OWLReasoner reasoner1 = reasonerFactory1.createReasoner(ontology1);

        for (OWLAxiom ax : ontology2.getTBoxAxioms(Imports.INCLUDED)) {
            if (reasoner1.isEntailed(ax)) {
                manager_w.addAxiom(witness, ax);
            }
        }

        if (witness != null) {
            System.out.println(witness.getTBoxAxioms(Imports.INCLUDED).size());
        }
        else {
            System.out.println("No witness");
        }
    }

    public void testSave() {
        snomed_ct main = new snomed_ct();

        ClassLoader classloader = Thread.currentThread().getContextClassLoader();
        OWLOntologyManager manager1 = OWLManager.createOWLOntologyManager();

        OWLOntology ontology1 = main.loadOntology(manager1,
                classloader.getResourceAsStream("ontology_201801.owl"));

        String path = "201801_test.owl";
        File out = new File(path);
        try {
            Files.deleteIfExists(out.toPath());
        } catch (IOException e) {
            e.printStackTrace();
        }
        IRI des_iri = IRI.create(out.toURI());
        System.out.println(des_iri);
        main.saveOntology(manager1, ontology1, des_iri);
    }

    public void testCreateSig() {
        snomed_ct main = new snomed_ct();

        ClassLoader classloader = Thread.currentThread().getContextClassLoader();
        OWLOntologyManager manager1 = OWLManager.createOWLOntologyManager();
        OWLOntologyManager manager2 = OWLManager.createOWLOntologyManager();

        OWLOntology ontology1 = main.loadOntology(manager1,
                classloader.getResourceAsStream("ontology_202001.owl"));
        OWLOntology ontology2 = main.loadOntology(manager2,
                classloader.getResourceAsStream("ontology_202007.owl"));

        Set<OWLClass> Concepts = ontology1.getClassesInSignature();
        Concepts.retainAll(ontology2.getClassesInSignature());
        List<OWLClass> randomSampledConcepts = new ArrayList<>(Concepts);
        Set<OWLObjectProperty> Roles = ontology1.getObjectPropertiesInSignature();
        Roles.retainAll(ontology1.getObjectPropertiesInSignature());
        List<OWLObjectProperty> randomSampledRoles = new ArrayList<>(Roles);

        String sigPath = "sig56.txt";


        File sigFile = new File(sigPath);
        if (!sigFile.exists()){
            try {
                sigFile.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        BufferedWriter sigWriter = null;
        try {
            sigWriter = new BufferedWriter(new FileWriter(sigFile));
        } catch (IOException e) {
            e.printStackTrace();
        }
        StringBuilder info = new StringBuilder();
        if (randomSampledConcepts.size() > 0) {
            for (OWLClass c : randomSampledConcepts) {
                info.append("Concept|").append(c.getIRI().toString()).append(",");
            }
        }
        if (randomSampledRoles.size() > 0) {
            for (int j = 0, sizeOfRandomSampledRoles = randomSampledRoles.size(); j < sizeOfRandomSampledRoles; j++) {
                if (j != sizeOfRandomSampledRoles-1)
                    info.append("Role|").append(randomSampledRoles.get(j).getIRI().toString()).append(",");
                else
                    info.append("Role|").append(randomSampledRoles.get(j).getIRI().toString());
            }
        }
//                        System.out.println(iter + "," + info);
        try {
            sigWriter.write(info + "\n");
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            sigWriter.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
