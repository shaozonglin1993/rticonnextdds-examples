/*******************************************************************************
 (c) 2005-2014 Copyright, Real-Time Innovations, Inc.  All rights reserved.
 RTI grants Licensee a license to use, modify, compile, and create derivative
 works of the Software.  Licensee has the right to distribute object form only
 for use with RTI products.  The Software is provided "as is", with no warranty
 of any type, including any warranty for fitness for any purpose. RTI is under
 no obligation to maintain or support the Software.  RTI shall not be liable for
 any incidental or consequential damages arising out of the use or inability to
 use the software.
 ******************************************************************************/

/* FooPublisher.java

   A publication of data of type Foo

   This file is derived from code automatically generated by the rtiddsgen 
   command:

   rtiddsgen -language java -example <arch> .idl

   Example publication of type Foo automatically generated by 
   'rtiddsgen' To test them follow these steps:

   (1) Compile this file and the example subscription.

   (2) Start the subscription with the command
       java FooSubscriber <domain_id> <sample_count>

   (3) Start the publication with the command
       java FooPublisher <domain_id> <sample_count>

   (4) [Optional] Specify the list of discovery initial peers and 
       multicast receive addresses via an environment variable or a file 
       (in the current working directory) called NDDS_DISCOVERY_PEERS.  

   You can run any number of publishers and subscribers programs, and can 
   add and remove them dynamically from the domain.

   Example:

       To run the example application on domain <domain_id>:

       Ensure that $(NDDSHOME)/lib/<arch> is on the dynamic library path for
       Java.                       

        On Unix: 
             add $(NDDSHOME)/lib/<arch> to the 'LD_LIBRARY_PATH' environment
             variable

        On Windows:
             add %NDDSHOME%\lib\<arch> to the 'Path' environment variable


       Run the Java applications:

        java -Djava.ext.dirs=$NDDSHOME/class FooPublisher <domain_id>

        java -Djava.ext.dirs=$NDDSHOME/class FooSubscriber <domain_id>        



modification history
------------ -------         
 */

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Arrays;

import com.rti.dds.domain.*;
import com.rti.dds.infrastructure.*;
import com.rti.dds.publication.*;
import com.rti.dds.topic.*;
import com.rti.ndds.config.*;

// ===========================================================================

public class FooPublisher {
    // -----------------------------------------------------------------------
    // Public Methods
    // -----------------------------------------------------------------------

    public static void main(String[] args) {
        // --- Get domain ID --- //
        int domainId = 0;
        if (args.length >= 1) {
            domainId = Integer.valueOf(args[0]).intValue();
        }

        // -- Get max loop count; 0 means infinite loop --- //
        int sampleCount = 0;
        if (args.length >= 2) {
            sampleCount = Integer.valueOf(args[1]).intValue();
        }

        /* Uncomment this to turn on additional logging
        Logger.get_instance().set_verbosity_by_category(
            LogCategory.NDDS_CONFIG_LOG_CATEGORY_API,
            LogVerbosity.NDDS_CONFIG_LOG_VERBOSITY_STATUS_ALL);
         */

        // --- Run --- //
        publisherMain(domainId, sampleCount);
    }



    // -----------------------------------------------------------------------
    // Private Methods
    // -----------------------------------------------------------------------

    // --- Constructors: -----------------------------------------------------

    private FooPublisher() {
        super();
    }


    // -----------------------------------------------------------------------

    private static void publisherMain(int domainId, int sampleCount) {

        DomainParticipant participant = null;
        Publisher publisher = null;
        Publisher publisher2 = null;
        Topic topic = null;
        FooDataWriter writer = null;

        try {
            // --- Create participant --- //

            /* To customize participant QoS, use
               the configuration file
               USER_QOS_PROFILES.xml */

            participant = DomainParticipantFactory.TheParticipantFactory.
                    create_participant(
                            domainId, 
                            DomainParticipantFactory.PARTICIPANT_QOS_DEFAULT,
                            null /* listener */, StatusKind.STATUS_MASK_NONE);
            if (participant == null) {
                System.err.println("create_participant error\n");
                return;
            }        

            // --- Create publisher --- //

            /* To customize publisher QoS, use
               the configuration file USER_QOS_PROFILES.xml */

            /* Start - modifying the generated example to showcase the usage of
             * the get_publishers API
             */   

            publisher = participant.create_publisher(
                    DomainParticipant.PUBLISHER_QOS_DEFAULT, 
                    null /* listener */,
                    StatusKind.STATUS_MASK_NONE);
            if (publisher == null) {
                System.err.println("create_publisher error\n");
                return;
            }
            System.out.println("The first publisher is " + publisher); 

            publisher2 = participant.create_publisher(
                    DomainParticipant.PUBLISHER_QOS_DEFAULT, 
                    null /* listener */,
                    StatusKind.STATUS_MASK_NONE);
            if (publisher2 == null) {
                System.err.println("create_publisher2 error\n");
                return;
            }                   
            System.out.println("The second publisher is " + publisher2); 

            System.out.println("Let's call get_publisher() now and check if I get all my publishers...\n");
            PublisherSeq publisherSeq = new PublisherSeq();
            participant.get_publishers(publisherSeq);

            System.out.println("I found " + publisherSeq.size() + "publishers");

            for (int i = 0; i < publisherSeq.size(); i++) {
                Publisher tmp = (Publisher) publisherSeq.get(i);
                System.out.println("The " + i + " publisher I found is " + tmp);
            }

            /* exiting */
            return;

            /* End - modifying the generated example to showcase the usage of
             * the get_publishers API
             */                

        } finally {

            // --- Shutdown --- //

            if(participant != null) {
                participant.delete_contained_entities();

                DomainParticipantFactory.TheParticipantFactory.
                delete_participant(participant);
            }
            /* RTI Connext provides finalize_instance()
               method for people who want to release memory used by the
               participant factory singleton. Uncomment the following block of
               code for clean destruction of the participant factory
               singleton. */
            //DomainParticipantFactory.finalize_instance();
        }
    }
}


