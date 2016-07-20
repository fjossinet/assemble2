import fr.unistra.ibmc.assemble2.io.FileParser
import fr.unistra.ibmc.assemble2.io.computations.Rnaview
import fr.unistra.ibmc.assemble2.model.AlignedMolecule
import fr.unistra.ibmc.assemble2.model.ReferenceStructure
import fr.unistra.ibmc.assemble2.model.StructuralAlignment
import fr.unistra.ibmc.assemble2.utils.Modeling2DUtils
import fr.unistra.ibmc.assemble2.utils.Modeling3DUtils

import javax.swing.JFileChooser
import javax.swing.filechooser.FileFilter

def openPDBDialog  = new JFileChooser(dialogTitle:"Choose a PDB file", fileSelectionMode : JFileChooser.FILES_ONLY,fileFilter: [getDescription: {-> "*.pdb"}, accept:{file-> file ==~ /.*?\.pdb/ || file.isDirectory() }] as FileFilter, currentDirectory: this.mediator.getAssemble().getLastWorkingDirectory())
if(openPDBDialog.showOpenDialog() == JFileChooser.APPROVE_OPTION) {

    this.mediator.getAssemble().setLastWorkingDirectory(openPDBDialog.selectedFile)

    tertiaryStructures = FileParser.parsePDB(new FileReader(openPDBDialog.selectedFile), null)
    secondaryStructure = new Rnaview(mediator).annotate(tertiaryStructures.get(0));
    def openFastaDialog  = new JFileChooser(dialogTitle:"Choose a FASTA file", fileSelectionMode : JFileChooser.FILES_ONLY,fileFilter: [getDescription: {-> "*.fasta"}, accept:{file-> file ==~ /.*?\.fasta/ || file.isDirectory() }] as FileFilter, currentDirectory: this.mediator.getAssemble().getLastWorkingDirectory())

    if(openFastaDialog.showOpenDialog() == JFileChooser.APPROVE_OPTION) {

        this.mediator.getAssemble().setLastWorkingDirectory(openFastaDialog.selectedFile)

        molecules = FileParser.parseFastaAsMolecules(new FileReader(openFastaDialog.selectedFile), null)

        AlignedMolecule referenceMolecule = new AlignedMolecule(this.mediator, tertiaryStructures.get(0).molecule)
        ReferenceStructure referenceStructure = new ReferenceStructure(referenceMolecule, secondaryStructure)
        i = 0
        for (molecule in molecules) {
            alignedMolecule = new AlignedMolecule(this.mediator, molecule)
            ss = Modeling2DUtils.infer2D(this.mediator, referenceStructure, alignedMolecule)
            alignedMolecules = new ArrayList<AlignedMolecule>()
            alignedMolecules.add(alignedMolecule)
            structuralAlignment = new StructuralAlignment(this.mediator, referenceMolecule, referenceStructure, alignedMolecules)
            ts = Modeling3DUtils.infer3D(structuralAlignment, alignedMolecule, tertiaryStructures.get(0))
            FileParser.writePDBFile(ts.getResidues3D(), true, new PrintWriter(new File(openFastaDialog.selectedFile.getParent(), molecule.name+"_"+i+".pdb")))
            i++
        }
    }


}
