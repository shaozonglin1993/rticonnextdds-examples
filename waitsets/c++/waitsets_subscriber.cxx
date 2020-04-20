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
/* waitsets_subscriber.cxx

   A subscription example

   This file is derived from code automatically generated by the rtiddsgen 
   command:

   rtiddsgen -language C++ -example <arch> waitsets.idl

   Example subscription of type waitsets automatically generated by 
   'rtiddsgen'. To test them follow these steps:

   (1) Compile this file and the example publication.

   (2) Start the subscription with the command
       objs/<arch>/waitsets_subscriber <domain_id> <sample_count>

   (3) Start the publication with the command
       objs/<arch>/waitsets_publisher <domain_id> <sample_count>

   (4) [Optional] Specify the list of discovery initial peers and 
       multicast receive addresses via an environment variable or a file 
       (in the current working directory) called NDDS_DISCOVERY_PEERS. 
       
   You can run any number of publishers and subscribers programs, and can 
   add and remove them dynamically from the domain.
              
                                   
   Example:
        
       To run the example application on domain <domain_id>:
                          
       On Unix: 
       
       objs/<arch>/waitsets_publisher <domain_id> 
       objs/<arch>/waitsets_subscriber <domain_id> 
                            
       On Windows:
       
       objs\<arch>\waitsets_publisher <domain_id>  
       objs\<arch>\waitsets_subscriber <domain_id>   
              
       
modification history
------------ -------       
*/

#include <stdio.h>
#include <stdlib.h>
#ifdef RTI_VX653
#include <vThreadsData.h>
#endif
#include "waitsets.h"
#include "waitsetsSupport.h"
#include "ndds/ndds_cpp.h"

/* We don't need to use listeners as we are going to use Waitsets and Conditions
 * so we have removed the auto generated code for listeners here */

/* Delete all entities */
static int subscriber_shutdown(
    DDSDomainParticipant *participant)
{
    DDS_ReturnCode_t retcode;
    int status = 0;

    if (participant != NULL) {
        retcode = participant->delete_contained_entities();
        if (retcode != DDS_RETCODE_OK) {
            printf("delete_contained_entities error %d\n", retcode);
            status = -1;
        }

        retcode = DDSTheParticipantFactory->delete_participant(participant);
        if (retcode != DDS_RETCODE_OK) {
            printf("delete_participant error %d\n", retcode);
            status = -1;
        }
    }

    /* RTI Connext provides the finalize_instance() method on
       domain participant factory for people who want to release memory used
       by the participant factory. Uncomment the following block of code for
       clean destruction of the singleton. */
/*
    retcode = DDSDomainParticipantFactory::finalize_instance();
    if (retcode != DDS_RETCODE_OK) {
        printf("finalize_instance error %d\n", retcode);
        status = -1;
    }
*/
    return status;
}

extern "C" int subscriber_main(int domainId, int sample_count)
{
    DDSDomainParticipant *participant = NULL;
    DDSSubscriber *subscriber = NULL;
    DDSTopic *topic = NULL;
    DDSDataReader *reader = NULL;
    DDS_ReturnCode_t retcode;
    const char *type_name = NULL;
    int count = 0;
    int status = 0;
    struct DDS_Duration_t wait_timeout = {1,500000000};

    /* To customize the participant QoS, use 
       the configuration file USER_QOS_PROFILES.xml */
    participant = DDSTheParticipantFactory->create_participant(
        domainId, DDS_PARTICIPANT_QOS_DEFAULT, 
        NULL /* listener */, DDS_STATUS_MASK_NONE);
    if (participant == NULL) {
        printf("create_participant error\n");
        subscriber_shutdown(participant);
        return -1;
    }

    /* To customize the subscriber QoS, use 
       the configuration file USER_QOS_PROFILES.xml */
    subscriber = participant->create_subscriber(
        DDS_SUBSCRIBER_QOS_DEFAULT, NULL /* listener */, DDS_STATUS_MASK_NONE);
    if (subscriber == NULL) {
        printf("create_subscriber error\n");
        subscriber_shutdown(participant);
        return -1;
    }

    /* Register the type before creating the topic */
    type_name = waitsetsTypeSupport::get_type_name();
    retcode = waitsetsTypeSupport::register_type(
        participant, type_name);
    if (retcode != DDS_RETCODE_OK) {
        printf("register_type error %d\n", retcode);
        subscriber_shutdown(participant);
        return -1;
    }

    /* To customize the topic QoS, use 
       the configuration file USER_QOS_PROFILES.xml */
    topic = participant->create_topic(
        "Example waitsets",
        type_name, DDS_TOPIC_QOS_DEFAULT, NULL /* listener */,
        DDS_STATUS_MASK_NONE);
    if (topic == NULL) {
        printf("create_topic error\n");
        subscriber_shutdown(participant);
        return -1;
    }

    /* To customize the data reader QoS, use 
       the configuration file USER_QOS_PROFILES.xml */
    reader = subscriber->create_datareader(
        topic, DDS_DATAREADER_QOS_DEFAULT, NULL,
        DDS_STATUS_MASK_NONE);
    if (reader == NULL) {
        printf("create_datareader error\n");
        subscriber_shutdown(participant);
        return -1;
    }

    /* If you want to change the DataReader's QoS programmatically rather than
     * using the XML file, you will need to add the following lines to your
     * code and comment out the create_datareader call above.
     *
     * In this case, we reduce the liveliness timeout period to trigger the
     * StatusCondition DDS_LIVELINESS_CHANGED_STATUS
     */

    /*
    DDS_DataReaderQos datareader_qos;
    retcode = subscriber->get_default_datareader_qos(datareader_qos);
    if (retcode != DDS_RETCODE_OK) {
        printf("get_default_datareader_qos error\n");
        return -1;
    }

    datareader_qos.liveliness.lease_duration.sec = 2;
    datareader_qos.liveliness.lease_duration.nanosec = 0;

    reader = subscriber->create_datareader(
        topic, datareader_qos, NULL,
        DDS_STATUS_MASK_NONE);
    if (reader == NULL) {
        printf("create_datareader error\n");
        subscriber_shutdown(participant);
        return -1;
    }
    */

    /* Create read condition
     * ---------------------
     * Note that the Read Conditions are dependent on both incoming
     * data as well as sample state. Thus, this method has more
     * overhead than adding a DDS_DATA_AVAILABLE_STATUS StatusCondition.
     * We show it here purely for reference
     */
    DDSReadCondition* read_condition = reader->create_readcondition(
        DDS_NOT_READ_SAMPLE_STATE,
        DDS_ANY_VIEW_STATE,
        DDS_ANY_INSTANCE_STATE);
    if (read_condition == NULL) {
        printf("create_readcondition error\n");
        subscriber_shutdown(participant);
        return -1;
    }

    /* Get status conditions
     * ---------------------
     * Each entity may have an attached Status Condition. To modify the
     * statuses we need to get the reader's Status Conditions first.
     */
    DDSStatusCondition* status_condition = reader->get_statuscondition();
    if (status_condition == NULL) {
        printf("get_statuscondition error\n");
        subscriber_shutdown(participant);
        return -1;
    }

    /* Set enabled statuses
     * --------------------
     * Now that we have the Status Condition, we are going to enable the
     * statuses we are interested in: DDS_SUBSCRIPTION_MATCHED_STATUS and
     * DDS_LIVELINESS_CHANGED_STATUS.
     */
    retcode = status_condition->set_enabled_statuses(
        DDS_SUBSCRIPTION_MATCHED_STATUS | DDS_LIVELINESS_CHANGED_STATUS);
    if (retcode != DDS_RETCODE_OK) {
        printf("set_enabled_statuses error\n");
        subscriber_shutdown(participant);
        return -1;
    }

    /* Create and attach conditions to the WaitSet
     * -------------------------------------------
     * Finally, we create the WaitSet and attach both the Read Conditions
     * and the Status Condition to it.
     */
    DDSWaitSet* waitset = new DDSWaitSet();

    /* Attach Read Conditions */
    retcode = waitset->attach_condition(read_condition);
    if (retcode != DDS_RETCODE_OK) {
        printf("attach_condition error\n");
        subscriber_shutdown(participant);
        return -1;
    }

    /* Attach Status Conditions */
    retcode = waitset->attach_condition(status_condition);
    if (retcode != DDS_RETCODE_OK) {
        printf("attach_condition error\n");
        subscriber_shutdown(participant);
        return -1;
    }

    /* Narrow the reader into your specific data type */
    waitsetsDataReader *waitsets_reader = waitsetsDataReader::narrow(reader);
    if (waitsets_reader == NULL) {
        printf("DataReader narrow error\n");
        return -1;
    }

    /* Main loop */
    for (count=0; (sample_count == 0) || (count < sample_count); ++count) {
        DDSConditionSeq active_conditions_seq;

        /* wait() blocks execution of the thread until one or more attached
         * Conditions become true, or until a user-specified timeout expires.
         */
        retcode = waitset->wait(active_conditions_seq, wait_timeout);
        /* We get to timeout if no conditions were triggered */
        if (retcode == DDS_RETCODE_TIMEOUT) {
            printf("Wait timed out!! No conditions were triggered.\n");
            continue;
        } else if (retcode != DDS_RETCODE_OK) {
            printf("wait returned error: %d\n", retcode);
            break;
        }

        /* Get the number of active conditions */
        int active_conditions = active_conditions_seq.length();
        printf("Got %d active conditions\n", active_conditions);

        for (int i = 0; i < active_conditions; ++i) {
            /* Now we compare the current condition with the Status
             * Conditions and the Read Conditions previously defined. If
             * they match, we print the condition that was triggered.*/

            /* Compare with Status Conditions */
            if (active_conditions_seq[i] == status_condition) {
                /* Get the status changes so we can check which status
                 * condition triggered. */
                DDS_StatusMask triggeredmask =
                        waitsets_reader->get_status_changes();

                /* Liveliness changed */
                if (triggeredmask & DDS_LIVELINESS_CHANGED_STATUS) {
                    DDS_LivelinessChangedStatus st;
                    waitsets_reader->get_liveliness_changed_status(st);
                    printf("Liveliness changed => Active writers = %d\n",
                           st.alive_count);
                }

                /* Subscription matched */
                if (triggeredmask & DDS_SUBSCRIPTION_MATCHED_STATUS) {
                    DDS_SubscriptionMatchedStatus st;
                    waitsets_reader->get_subscription_matched_status(st);
                    printf("Subscription matched => Cumulative matches = %d\n",
                           st.total_count);
                }
            }

            /* Compare with Read Conditions */
            else if (active_conditions_seq[i] == read_condition) {
                /* Current conditions match our conditions to read data, so
                 * we can read data just like we would do in any other
                 * example. */
                waitsetsSeq data_seq;
                DDS_SampleInfoSeq info_seq;

                /* You may want to call take_w_condition() or
                 * read_w_condition() on the Data Reader. This way you will use
                 * the same status masks that were set on the Read Condition.
                 * This is just a suggestion, you can always use
                 * read() or take() like in any other example.
                 */
                retcode = DDS_RETCODE_OK;
                while (retcode != DDS_RETCODE_NO_DATA) {
                    retcode = waitsets_reader->take_w_condition(
                        data_seq, info_seq, DDS_LENGTH_UNLIMITED,
                                                        read_condition);
                    
                    for (int j = 0; j < data_seq.length(); ++j) {
                        if (!info_seq[j].valid_data) {
                            printf("Got metadata\n");
                            continue;
                        }
                        waitsetsTypeSupport::print_data(&data_seq[j]);
                    }
                    waitsets_reader->return_loan(data_seq, info_seq);

                }
               
            }

        }

    }

    /* Delete all entities */
    delete waitset;

    status = subscriber_shutdown(participant);

    return status;
}

#if defined(RTI_WINCE)
int wmain(int argc, wchar_t** argv)
{
    int domainId = 0;
    int sample_count = 0; /* infinite loop */ 
    
    if (argc >= 2) {
        domainId = _wtoi(argv[1]);
    }
    if (argc >= 3) {
        sample_count = _wtoi(argv[2]);
    }
    
    /* Uncomment this to turn on additional logging
    NDDSConfigLogger::get_instance()->
        set_verbosity_by_category(NDDS_CONFIG_LOG_CATEGORY_API, 
                                  NDDS_CONFIG_LOG_VERBOSITY_STATUS_ALL);
    */
                                  
    return subscriber_main(domainId, sample_count);
}

#elif !(defined(RTI_VXWORKS) && !defined(__RTP__)) && !defined(RTI_PSOS)
int main(int argc, char *argv[])
{
    int domainId = 0;
    int sample_count = 0; /* infinite loop */

    if (argc >= 2) {
        domainId = atoi(argv[1]);
    }
    if (argc >= 3) {
        sample_count = atoi(argv[2]);
    }


    /* Uncomment this to turn on additional logging
    NDDSConfigLogger::get_instance()->
        set_verbosity_by_category(NDDS_CONFIG_LOG_CATEGORY_API, 
                                  NDDS_CONFIG_LOG_VERBOSITY_STATUS_ALL);
    */
                                  
    return subscriber_main(domainId, sample_count);
}
#endif

#ifdef RTI_VX653
const unsigned char* __ctype = *(__ctypePtrGet());

extern "C" void usrAppInit ()
{
#ifdef  USER_APPL_INIT
    USER_APPL_INIT;         /* for backwards compatibility */
#endif
    
    /* add application specific code here */
    taskSpawn("sub", RTI_OSAPI_THREAD_PRIORITY_NORMAL, 0x8, 0x150000,
            (FUNCPTR)subscriber_main, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0);
   
}
#endif

