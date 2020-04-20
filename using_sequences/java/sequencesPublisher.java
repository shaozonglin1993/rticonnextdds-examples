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

/* sequencesPublisher.java

   A publication of data of type sequences

   This file is derived from code automatically generated by the rtiddsgen 
   command:

   rtiddsgen -language java -example <arch> .idl

   Example publication of type sequences automatically generated by 
   'rtiddsgen' To test them follow these steps:

   (1) Compile this file and the example subscription.

   (2) Start the subscription with the command
       java sequencesSubscriber <domain_id> <sample_count>
       
   (3) Start the publication with the command
       java sequencesPublisher <domain_id> <sample_count>

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
       
        java -Djava.ext.dirs=$NDDSHOME/class sequencesPublisher <domain_id>

        java -Djava.ext.dirs=$NDDSHOME/class sequencesSubscriber <domain_id>        

       
       
modification history
------------ -------         
*/

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.Random;

import com.rti.dds.domain.*;
import com.rti.dds.infrastructure.*;
import com.rti.dds.publication.*;
import com.rti.dds.topic.*;
import com.rti.ndds.config.*;

// ===========================================================================

public class sequencesPublisher {
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
    
    private sequencesPublisher() {
        super();
    }
    
    
    // -----------------------------------------------------------------------
    
    private static void publisherMain(int domainId, int sampleCount) {

        DomainParticipant participant = null;
        Publisher publisher = null;
        Topic topic = null;
        sequencesDataWriter writer = null;

        try {
            // --- Create participant --- //
    
            /* To customize participant QoS, use
               the configuration file
               USER_QOS_PROFILES.xml */
    
            participant = DomainParticipantFactory.TheParticipantFactory.
                create_participant(
                    domainId, DomainParticipantFactory.PARTICIPANT_QOS_DEFAULT,
                    null /* listener */, StatusKind.STATUS_MASK_NONE);
            if (participant == null) {
                System.err.println("create_participant error\n");
                return;
            }        
                    
            // --- Create publisher --- //
    
            /* To customize publisher QoS, use
               the configuration file USER_QOS_PROFILES.xml */
    
            publisher = participant.create_publisher(
                DomainParticipant.PUBLISHER_QOS_DEFAULT, null /* listener */,
                StatusKind.STATUS_MASK_NONE);
            if (publisher == null) {
                System.err.println("create_publisher error\n");
                return;
            }                   
                
        
            // --- Create topic --- //

            /* Register type before creating topic */
            String typeName = sequencesTypeSupport.get_type_name();
            sequencesTypeSupport.register_type(participant, typeName);
    
            /* To customize topic QoS, use
               the configuration file USER_QOS_PROFILES.xml */
    
            topic = participant.create_topic(
                "Example sequences",
                typeName, DomainParticipant.TOPIC_QOS_DEFAULT,
                null /* listener */, StatusKind.STATUS_MASK_NONE);
            if (topic == null) {
                System.err.println("create_topic error\n");
                return;
            }           
                
            // --- Create writer --- //
    
            /* To customize data writer QoS, use
               the configuration file USER_QOS_PROFILES.xml */
    
            writer = (sequencesDataWriter)
                publisher.create_datawriter(
                    topic, Publisher.DATAWRITER_QOS_DEFAULT,
                    null /* listener */, StatusKind.STATUS_MASK_NONE);
            if (writer == null) {
                System.err.println("create_datawriter error\n");
                return;
            }           
                                        
            // --- Write --- //

            /* Here we define two instances of sequences: ownerInstance and 
             * borrowerInstance. */

            /* ownerInstance.data uses its own memory as, by default, a sequence
             * you create owns its memory unless you explicitly loan memory of 
             * your own to it. */
            sequences ownerInstance = new sequences();
            sequences borrowerInstance = new sequences();

            InstanceHandle_t ownerInstanceHandle = 
            		InstanceHandle_t.HANDLE_NIL;
            InstanceHandle_t borrowerInstanceHandle = 
            		InstanceHandle_t.HANDLE_NIL;            

            /* If we want borrower_instance.data to loan a buffer of shorts, 
             * first we have to allocate the buffer. Here we allocate a buffer 
             * of MAX_SEQUENCE_LEN. */
            short[] shortBuffer = new short[MAX_SEQUENCE_LEN.VALUE];
            
            /* Before calling loan(), we need to set sequence maximum to 0, 
             * i.e., the sequence won't have memory allocated to it. */
            borrowerInstance.data.setMaximum(0);

            /* Now that the sequence doesn't have memory allocated to it, we can
             * call loan() to loan shortBuffer to borrowerInstance.
             * We set the allocated number of elements to MAX_SEQUENCE_LEN, the 
             * size of the buffer and the maximum size of the sequence as we 
             * declared in the IDL. */
            borrowerInstance.data.loan(
            		shortBuffer,	// Buffer
            		0 				// Initial Length
            		);
            
            /* Before starting to publish samples, set the instance id of each
             * instance*/
            ownerInstance.id 	= "owner_instance";
            borrowerInstance.id = "browser_instance";

            /* Send a new sample every second */
            final long sendPeriodMillis = 1 * 1000; // 1 second
            
            /* We use Random to generate random values for the sequences */
            Random rand = new Random();

            /* To illustrate the use of the sequences, in the main loop we set a
             * new sequence length every iteration to the sequences contained in
             * both instances (instance.data). The sequence length value cycles
             * between 0 and MAX_SEQUENCE_LEN. We assign a random number between
             * 0 and 100 to each sequence's elements. */
            for (int count = 0;
                 (sampleCount == 0) || (count < sampleCount);
                 ++count) {
            	
                /* We set a different sequenceLength for both instances every
                 * iteration. sequenceLength is based on the value of count 
                 * and its value cycles between the values of 1 and 
                 * MAX_SEQUENCE_LEN. */
            	int sequenceLength = (count % MAX_SEQUENCE_LEN.VALUE) + 1;
            	
                System.out.println("Writing sequences, count " + count);
                
                ownerInstance.count = (short) count;
                borrowerInstance.count = (short) count;

                /* Here we set the new length of each sequence */
                ownerInstance.data.setSize(sequenceLength);
                borrowerInstance.data.setSize(sequenceLength);

                /* Now that the sequences have a new length, we assign a
                 * random number between 0 and 100 to each element of
                 * ownerInstance.data and borrowerInstance.data. */
                for (int i = 0; i < sequenceLength; ++i) {
                    ownerInstance.data.setShort(i, 
                    		(short) rand.nextInt(100)
                    		);
                    borrowerInstance.data.setShort(i, 
                    		(short) rand.nextInt(100)
                    		);                	
                }

                /* Both sequences have the same length, so we only print the 
                 * length of one of them. */
                System.out.println("Instances length = " +
                		ownerInstance.data.size()
                		);

                /* Write data */
                writer.write(ownerInstance, ownerInstanceHandle);
                try {
                    Thread.sleep(sendPeriodMillis);
                } catch (InterruptedException ix) {
                    System.err.println("INTERRUPTED");
                    break;
                }
                
                writer.write(borrowerInstance, borrowerInstanceHandle);
                try {
                    Thread.sleep(sendPeriodMillis);
                } catch (InterruptedException ix) {
                    System.err.println("INTERRUPTED");
                    break;
                }
                
            }
            
            /* Once we are done with the sequence, we call unloan() */
            borrowerInstance.data.unloan();
            
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

        
