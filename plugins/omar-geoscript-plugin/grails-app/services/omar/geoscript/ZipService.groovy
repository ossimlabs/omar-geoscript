
package omar.geoscript

import javax.inject.Singleton

@Singleton
class ZipService {

    String run(String dirToZip, String zipFileOutputName){

        log.info "Directory to be zipped: ${dirToZip}"
        log.info "Zip file output name: ${zipFileOutputName}"

        def ant = new AntBuilder()
        ant.zip(destfile: "${dirToZip}/${zipFileOutputName}.zip", basedir: dirToZip)

    }

}