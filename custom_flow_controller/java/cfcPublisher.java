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

/* cfcPublisher.java

   A publication of data of type cfc

   This file is derived from code automatically generated by the rtiddsgen 
   command:

   rtiddsgen -language java -example <arch> .idl

   Example publication of type cfc automatically generated by 
   'rtiddsgen' To test them follow these steps:

   (1) Compile this file and the example subscription.

   (2) Start the subscription with the command
       java cfcSubscriber <domain_id> <sample_count>
       
   (3) Start the publication with the command
       java cfcPublisher <domain_id> <sample_count>

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
       
        java -Djava.ext.dirs=$NDDSHOME/class cfcPublisher <domain_id>

        java -Djava.ext.dirs=$NDDSHOME/class cfcSubscriber <domain_id>        

       
       
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

public class cfcPublisher {
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
    
    private cfcPublisher() {
        super();
    }
    
    
    // -----------------------------------------------------------------------
    
    private static void publisherMain(int domainId, int sampleCount) {

        DomainParticipant participant = null;
        Publisher publisher = null;
        Topic topic = null;
        cfcDataWriter writer = null;

        try {
            // --- Create participant --- //
    
            participant = DomainParticipantFactory.TheParticipantFactory
                    .create_participant(domainId,
                            DomainParticipantFactory.PARTICIPANT_QOS_DEFAULT,
                            null /* listener */, StatusKind.STATUS_MASK_NONE);
            if (participant == null) {
                System.err.println("create_participant error\n");
                return;
            }     
                    
            
            /* Start changes for custom_flowcontroller */

            /* If you want to change the Participant's QoS programmatically 
             * rather than using the XML file, you will need to add the 
             * following lines to your code and comment out the 
             * create_participant call above.
             */
/*            
            DomainParticipantQos participant_qos = new DomainParticipantQos();
            DomainParticipantFactory.TheParticipantFactory.
                    get_default_participant_qos(participant_qos);

            // By default, data will be sent via shared memory _and_ UDPv4.  
            // Because the flowcontroller limits writes across all interfaces, 
            // this halves the effective send rate.  To avoid this, we enable 
            // only the UDPv4 transport
            participant_qos.transport_builtin.mask = TransportBuiltinKind.UDPv4;

            // To create participant with default QoS, use 
            // DomainParticipantFactory.PARTICIPANT_QOS_DEFAULT instead of 
            // participant_qos
            participant = DomainParticipantFactory.TheParticipantFactory.
                create_participant(
                    domainId, participant_qos,
                    null, StatusKind.STATUS_MASK_NONE);
*/
            /* End changes for Custom_Flowcontroller */

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
            String typeName = cfcTypeSupport.get_type_name();
            cfcTypeSupport.register_type(participant, typeName);
    
            /* To customize topic QoS, use
               the configuration file USER_QOS_PROFILES.xml */
    
            topic = participant.create_topic(
                "Example cfc",
                typeName, DomainParticipant.TOPIC_QOS_DEFAULT,
                null /* listener */, StatusKind.STATUS_MASK_NONE);
            if (topic == null) {
                System.err.println("create_topic error\n");
                return;
            }           
                
            // --- Create writer --- //
    
            /* To customize data writer QoS, use
               the configuration file USER_QOS_PROFILES.xml */
    
            writer = (cfcDataWriter)
                publisher.create_datawriter(
                    topic, Publisher.DATAWRITER_QOS_DEFAULT,
                    null /* listener */, StatusKind.STATUS_MASK_NONE);
            if (writer == null) {
                System.err.println("create_datawriter error\n");
                return;
            }           
            
            
            /* Start changes for custom_flowcontroller */

            /* If you want to change the Datawriter's QoS programmatically 
             * rather than using the XML file, you will need to add the 
             * following lines to your code and comment out the 
             * create_datawriter call above.
             *
             * In this case we create the flowcontroller and the neccesary QoS
             * for the datawriter.
             */
/*            
            String cfc_name = "custom_flowcontroller";
            // Create and configure flowcontroller properties
            FlowControllerProperty_t custom_fcp = 
                    new FlowControllerProperty_t();
            participant.get_default_flowcontroller_property(custom_fcp);

            // Don't allow too many tokens to accumulate
            custom_fcp.token_bucket.max_tokens = 
                custom_fcp.token_bucket.tokens_added_per_period = 2;
            custom_fcp.token_bucket.tokens_leaked_per_period = 
                    ResourceLimitsQosPolicy.LENGTH_UNLIMITED;

            // 100ms
            custom_fcp.token_bucket.period.sec = 0;
            custom_fcp.token_bucket.period.nanosec = 100000000;

            // The sample size is 1000, but the minimum bytes_per_token is 1024.
            // Furthermore, we want to allow some overhead.
            custom_fcp.token_bucket.bytes_per_token = 1024;

            // So, in summary, each token can be used to send about one message,
            // and we get 2 tokens every 100ms, so this limits transmissions to
            // about 20 messages per second.

            // Create flowcontroller and set properties
            FlowController cfc = participant.create_flowcontroller(
                    cfc_name, custom_fcp);
            cfc.set_property(custom_fcp);

            // --- Create writer --- //

            // Get default datawriter QoS to customize 
            DataWriterQos datawriter_qos = new DataWriterQos();
            publisher.get_default_datawriter_qos(datawriter_qos);

            // As an alternative to increasing history depth, we can just
            // set the qos to keep all samples
            datawriter_qos.history.kind =
                    HistoryQosPolicyKind.KEEP_ALL_HISTORY_QOS;
            
            // Set flowcontroller for datawriter
            datawriter_qos.publish_mode.kind = 
                    PublishModeQosPolicyKind.ASYNCHRONOUS_PUBLISH_MODE_QOS;
            datawriter_qos.publish_mode.flow_controller_name = cfc_name;

            writer = (cfcDataWriter)
                    publisher.create_datawriter(
                        topic, datawriter_qos,
                        null, StatusKind.STATUS_MASK_NONE);
*/                        
            /* End changes to custom_flowcontroller
            
            // --- Write --- //

            /* Create data sample for writing */
            cfc instance = new cfc();

            InstanceHandle_t instance_handle = InstanceHandle_t.HANDLE_NIL;
            /* For a data type that has a key, if the same instance is 
             * going to be written multiple times, initialize the key here
             * and register the keyed instance prior to writing */
            //instance_handle = writer.register_instance(instance);

            long sendPeriodMillis = 1 * 1000; // 4 seconds

            char str[] = new char[1000];
            for (int i = 0; i < 1000; ++i)
                str[i] = 'a';
            String data = new String(str);
            
            for (int count = 0;
                 (sampleCount == 0) || (count < sampleCount);
                 ++count) {
                
                /* Changes for Custom_Flowcontroller */
                /* Simulate bursty writer */
                
                try {
                    Thread.sleep(sendPeriodMillis);
                } catch (InterruptedException ix) {
                    System.err.println("INTERRUPTED");
                    break;
                }

                for (int i = 0; i < 10; ++i) {
                    int sample = count * 10 + i;
                    System.out.println("Writing cfc, sample " + sample);
                    instance.x = sample;
                    instance.str = data;
                    try {
                        writer.write(instance, instance_handle);
                    } catch (Exception e) {
                        System.out.println ("writer error: " + e);
                    }
                }
            }
             
            sendPeriodMillis = 4 * 1000;
            
            try {
                Thread.sleep(sendPeriodMillis);
            } catch (InterruptedException ix) {
                System.err.println("INTERRUPTED");
            }

            //writer.unregister_instance(instance, instance_handle);

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

        