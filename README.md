# EAGLET

EAGLET is a tool that supports the semi-automatic checking of a gold standard based on a set of uniform annotation rules. These set of rules are discussed in detail in the [paper](https://svn.aksw.org/papers/2017/ESWC_EAGLET_2017/public.pdf).

## Getting Started

Setting up EAGLET is a simple process.
### Prerequisites

* One just has to install Maven and EAGLET can be up and running. The instructions to install maven can be found [here](https://maven.apache.org/guides/getting-started/maven-in-five-minutes.html).
* Download the default results from [here]().

### Installing

Once the maven is up and running. Clone the repository using the following command in a desired folder

```
git clone https://github.com/AKSW/Eaglet.git
```
Once the repository is cloned. Copy the downloaded datasets to eaglet_data/result_pipe folder. Navigate to the base folder of the project and run the following command

```
mvn clean tomcat:run
```
EAGLET should be running at a default port 8080. Web interface can be seen at 
```
http://localhost:8080/gscheck/login.html
```


## Login
Enter a username Login to start playing with the current dataset. A detailed description of UI can be seen in the [demo](). One can also upload their own dataset as described in the next section. 

## Configuring the pipe
In order to check your own dataset, go to the Pipe Configuration Section and enter the detail and the local path to the dataset and press the run pipe. 

## Result
Once all the datasets have been reviewed the results can be found in eaglet_data/result_user/{username}.
## Built With

* [Spring](https://projects.spring.io/spring-framework/) - The web framework used
* [Maven](https://maven.apache.org/) - Dependency Management




## Authors

* [Kunal Jha](https://github.com/Kunal-Jha)
* [Michael Röder](https://github.com/MichaelRoeder)


## Acknowledgments
This work has been supported by the H2020 project [HOBBIT (GA no. 688227)](http://project-hobbit.eu) as well
as the the EuroStars projects [DIESEL (project no. 01QE1512C)](https://diesel-project.eu/) and [QAMEL (project
no. 01QE1549C)](https://qamel.eu/).
## License
EAGLET is licensed under the [GNU General Public License Version 2, June 1991](http://www.gnu.org/licenses/gpl-2.0.txt) (license document is in the application subfolder).
