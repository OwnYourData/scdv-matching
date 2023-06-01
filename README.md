# SCDV Matching
version 0.1.0 (10.04.2023)

Build instructions:  
* `mvn clean install`
* `java -jar target/scdv-matching-0.1.0-SNAPSHOT-jar-with-dependencies.jar`


## Usage Policy Matching Service  

### Instruction:  
The service is available at   
  - local deployment: `http://localhost:2806/api/validate/usagepolicy`
  - public instance: `https://scdv-matching-oc3.data-container.net/api/validate/usagepolicy`

Example usage: `curl -s -o /dev/null -w "%{http_code}" -H "Content-Type: application/json" -d "$(< src/test/resources/dpv/tester.json)" -X POST http://localhost:2806/api/validate/usagepolicy`  

### Input file format & response

The input file is of type `application/json` to wrap a turtle file using JSON key "usage-policy".  
There are four example turtle files in the `test/resources/dpv` directory for your references.

The output is `200` if the policy of controllers is not violating the policy of users, or `500` otherwise.

### Further Information

Find out more about the Data Privacy Vocabulary here: https://w3c.github.io/dpv/dpv/    
The current implementation was tested against v0.4 of the DPV.


## Issues

Please report bugs and suggestions for new features using the [GitHub Issue-Tracker](https://github.com/OwnYourData/dc-babelfish/issues) and follow the [Contributor Guidelines](https://github.com/twbs/ratchet/blob/master/CONTRIBUTING.md).

If you want to contribute, please follow these steps:

1. Fork it!
2. Create a feature branch: `git checkout -b my-new-feature`
3. Commit changes: `git commit -am 'Add some feature'`
4. Push into branch: `git push origin my-new-feature`
5. Send a Pull Request

&nbsp;    

## About  

<img align="right" src="https://raw.githubusercontent.com/OwnYourData/dc-babelfish/main/res/logo-ngi-ontochain-positive.png" height="150">This project has received funding from the European Union’s Horizon 2020 research and innovation program through the [NGI ONTOCHAIN program](https://ontochain.ngi.eu/) under cascade funding agreement No 957338.

<br clear="both" />

## License

[MIT License 2023 - OwnYourData.eu](https://raw.githubusercontent.com/OwnYourData/dc-babelfish/main/LICENSE)
