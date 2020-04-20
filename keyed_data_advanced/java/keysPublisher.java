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

/* keysPublisher.java

   A publication of data of type keys

   This file is derived from code automatically generated by the rtiddsgen 
   command:

   rtiddsgen -language java -example <arch> .idl

   Example publication of type keys automatically generated by 
   'rtiddsgen' To test them follow these steps:

   (1) Compile this file and the example subscription.

   (2) Start the subscription with the command
       java keysSubscriber <domain_id> <sample_count>

   (3) Start the publication with the command
       java keysPublisher <domain_id> <sample_count>

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

        java -Djava.ext.dirs=$NDDSHOME/class keysPublisher <domain_id>

        java -Djava.ext.dirs=$NDDSHOME/class keysSubscriber <domain_id>        



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

public class keysPublisher {
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

    private keysPublisher() {
        super();
    }


    // -----------------------------------------------------------------------

    private static void publisherMain(int domainId, int sampleCount) {

        DomainParticipant participant = null;
        Publisher publisher = null;
        Topic topic = null;
        keysDataWriter writer = null;

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
            String typeName = keysTypeSupport.get_type_name();
            keysTypeSupport.register_type(participant, typeName);

            /* To customize topic QoS, use
               the configuration file USER_QOS_PROFILES.xml */

            topic = participant.create_topic(
                    "Example keys",
                    typeName, DomainParticipant.TOPIC_QOS_DEFAULT,
                    null /* listener */, StatusKind.STATUS_MASK_NONE);
            if (topic == null) {
                System.err.println("create_topic error\n");
                return;
            }           

            // --- Create writers --- //

            /* We are going to load different QoS profiles for the two DWs */
            DataWriterQos datawriter_qos = new DataWriterQos();

            /* To customize data writer QoS, use
               the configuration file USER_QOS_PROFILES.xml */    
            writer = (keysDataWriter)
                    publisher.create_datawriter(
                            topic, Publisher.DATAWRITER_QOS_DEFAULT,
                            null /* listener */, StatusKind.STATUS_MASK_NONE);

            DomainParticipantFactory.TheParticipantFactory.get_datawriter_qos_from_profile(datawriter_qos,"keys_Library","keys_Profile_dw2");

            /* If you want to set the writer_data_lifecycle QoS settings
             * programmatically rather than using the XML, you will need to add
             * the following lines to your code and comment out the create_datawriter
             * and get_datawriter_qos_from_profile calls above.
             */

            /*
            publisher.get_default_datawriter_qos(datawriter_qos);

            datawriter_qos.writer_data_lifecycle.autodispose_unregistered_instances = false;
            datawriter_qos.ownership.kind = OwnershipQosPolicyKind.EXCLUSIVE_OWNERSHIP_QOS;
            datawriter_qos.ownership_strength.value = 10;

            writer = (keysDataWriter)
                    publisher.create_datawriter(
                            topic, datawriter_qos,
                            null, StatusKind.STATUS_MASK_NONE);

            datawriter_qos.ownership_strength.value = 5;
            */

            keysDataWriter writer2 = (keysDataWriter)
                    publisher.create_datawriter(
                            topic, datawriter_qos,
                            null /* listener */, StatusKind.STATUS_MASK_NONE);

            if (writer == null) {
                System.err.println("create_datawriter error\n");
                return;
            }           

            if (writer2 == null) {
                System.err.println("create_datawriter error\n");
                return;
            }  

            // --- Write --- //

            /* Creates three instances */
            keys instance[] = new keys[3];

            /* Create data samples for writing */
            instance[0] = new keys();
            instance[1] = new keys();
            instance[2] = new keys();

            /* RTI Connext could examine the key fields each time it needs to determine
             * which data-instance is being modified.
             * However, for performance and semantic reasons, it is better
             * for your application to declare all the data-instances it intends to
             * modify prior to actually writing any samples. This is known as registration.
             */

            /* In order to register the instances, we must set their associated keys first */
            instance[0].code = 0;
            instance[1].code = 1;
            instance[2].code = 2;

            /* Creates three handles for managing the registrations */
            InstanceHandle_t handle[] = new InstanceHandle_t[3];
            handle[0] = InstanceHandle_t.HANDLE_NIL;
            handle[1] = InstanceHandle_t.HANDLE_NIL;
            handle[2] = InstanceHandle_t.HANDLE_NIL;            

            /* The keys must have been set before making this call */
            System.out.print("----DW1 registering instance " + instance[0].code + "\n"); 
            handle[0] = writer.register_instance(instance[0]);

            /* Modify the data to be sent here */
            instance[0].x = 1;
            instance[1].x = 1;
            instance[2].x = 1;

            /* We only will send data over the instances marked as active */
            boolean active[] = {true, false, false}; 

            /* Make variables for the instance for the second datawriter to use.
               Note that it actually refers to the same logical instance, but
               because we're running both datawriters in the same thread, we
               to create separate variables so they don't clobber each other.
             */            
            keys instance_dw2 = new keys();

            /* instance_dw2 and instance[1] have the same key, and thus
            will write to the same instance (ins1). */
            instance_dw2.code = instance[1].code;
            instance_dw2.x = 2;
            InstanceHandle_t handle_dw2 = writer2.register_instance(instance_dw2);
            boolean active_dw2 = true;

            final long sendPeriodMillis = 1 * 1000; // 1 second.

            /* Main loop */
            for (int count = 0; (sampleCount == 0) || (count < sampleCount); ++count) {
                try {
                    Thread.sleep(sendPeriodMillis);
                } catch (InterruptedException ix) {
                    System.err.println("INTERRUPTED");
                    break;
                }
                /* Modify the instance to be written here */
                instance[0].y = count;
                instance[1].y = count + 1000;
                instance[2].y = count + 2000;

                instance_dw2.y = -count - 1000;

                /* We control two datawriters via a state machine here rather than
                   introducing separate threads.
                 */

                /* Control first DataWriter */
                switch (count) {
                case 4: { /* Start sending the second (ins1) and third instances (ins2) */
                    System.out.print("----DW1 registering instance " + instance[1].code + "\n"); 
                    System.out.print("----DW1 registering instance " + instance[2].code + "\n"); 
                    handle[1] = writer.register_instance(instance[1]);
                    handle[2] = writer.register_instance(instance[2]);
                    active[1] = true;
                    active[2] = true;
                } break;
                case 8: { /* Dispose the second instance (ins1) */
                    System.out.print("----DW1 disposing instance " + instance[1].code + "\n"); 
                    writer.dispose(instance[1], handle[1]);
                    active[1] = false;
                } break;
                case 10: { /* Unregister the second instance (ins1) */
                    System.out.print("----DW1 unregistering instance " + instance[1].code + "\n"); 
                    writer.unregister_instance(instance[1], handle[1]);
                    active[1] = false;
                } break;
                case 12: { /* Unregister the third instance (ins2) */
                    System.out.print("----DW1 unregistering instance " + instance[2].code + "\n"); 
                    writer.unregister_instance(instance[2], handle[2]);
                    active[2] = false;
                    /* Re-register the second instance (ins1) */
                    System.out.print("----DW1 re-registering instance " + instance[1].code + "\n"); 
                    handle[1] = writer.register_instance(instance[1]);
                    active[1] = true;
                } break;
                case 16: { /* Re-register the third instance (ins2) */
                    System.out.print("----DW1 re-registering instance " + instance[2].code + "\n"); 
                    writer.register_instance(instance[2]);
                    active[2] = true;
                } break;
                }

                for (int i = 0; i < 3; ++i) {
                    if (active[i]) {
                        System.out.print("DW1 write; code: " + instance[i].code + ", x: "
                                + instance[i].x + ", y: " + instance[i].y + "\n");
                        writer.write(instance[i], handle[i]);
                    }
                }

                /* Control second datawriter */
                switch (count) {
                case 16: { /* Dispose the instance (ins1). 
                        Since it has lower ownership strength, this does nothing */
                    System.out.print("----DW2 disposing instance " + instance_dw2.code + "\n"); 
                    writer2.dispose(instance_dw2, handle_dw2);
                    active_dw2 = false;
                } break;
                }

                if (active_dw2) {
                    System.out.print("DW2 write; code: "
                            + instance_dw2.code + ", x: " + instance_dw2.x + ", y: " 
                            + instance_dw2.y + "\n");
                    writer2.write(instance_dw2, handle_dw2);
                }

            }

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

