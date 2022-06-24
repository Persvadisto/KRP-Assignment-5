package owlapi.snomed_ct;

import com.clarkparsia.pellet.owlapiv3.PelletReasonerFactory;
import ontology_extraction.Utils;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.formats.OWLXMLDocumentFormat;
import org.semanticweb.owlapi.model.*;
import org.semanticweb.owlapi.model.parameters.Imports;
import org.semanticweb.owlapi.reasoner.OWLReasoner;
import org.semanticweb.owlapi.reasoner.OWLReasonerFactory;

import java.io.*;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static view.ViewComputer.computeViews;


public class snomed_ct {

    public snomed_ct() {}

    public OWLOntology loadOntology(OWLOntologyManager manager, InputStream inputStream) {
        OWLOntology owl;
        try {
            owl = manager.loadOntologyFromOntologyDocument(inputStream);
            return owl;
        } catch (OWLOntologyCreationException e) {
            e.printStackTrace();
        }
        return null;
    }

    public void saveOntology(OWLOntologyManager manager, OWLOntology ontology, IRI locationIRI) {
        try {
            manager.saveOntology(ontology, new OWLXMLDocumentFormat(), locationIRI);
        } catch (OWLOntologyStorageException e) {
            e.printStackTrace();
        }
    }

    public void diff(OWLOntology onto1, OWLOntology onto2, String path) {
        OWLOntologyManager manager_w = OWLManager.createOWLOntologyManager();
        OWLOntology witness = null;
        try {
            witness = manager_w.createOntology();
        } catch (OWLOntologyCreationException e) {
            e.printStackTrace();
        }

        OWLReasonerFactory reasonerFactory1 = new PelletReasonerFactory();
        OWLReasoner reasoner1 = reasonerFactory1.createReasoner(onto1);
        reasoner1.precomputeInferences();

        for (OWLAxiom ax : onto2.getTBoxAxioms(Imports.INCLUDED)) {
            if (!reasoner1.isEntailed(ax)) {
                manager_w.addAxiom(witness, ax);
            }
        }

        if (witness != null) {
            System.out.println(witness.getAxiomCount());

            OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
            File out = new File(path);
            try {
                Files.deleteIfExists(out.toPath());
            } catch (IOException e) {
                e.printStackTrace();
            }
            IRI des_iri = IRI.create(out.toURI());
            saveOntology(manager, witness, des_iri);
        }
        else {
            System.out.println("No witness");
        }
    }

    public void createSig(OWLOntology onto1, OWLOntology onto2, String sigPath) {
        Set<OWLClass> Concepts = onto1.getClassesInSignature();
        Concepts.retainAll(onto2.getClassesInSignature());
        List<OWLClass> randomSampledConcepts = new ArrayList<>(Concepts);
        Set<OWLObjectProperty> Roles = onto1.getObjectPropertiesInSignature();
        Roles.retainAll(onto1.getObjectPropertiesInSignature());
        List<OWLObjectProperty> randomSampledRoles = new ArrayList<>(Roles);


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

    public void sig_view(OWLOntology onto, String sigPath, String savePath) {
        try {
            Set<OWLEntity> sigma = Utils.readSignatures(sigPath);
            computeViews(sigma, onto, savePath);
        } catch (Exception e){
            e.printStackTrace();
        }
    }
}