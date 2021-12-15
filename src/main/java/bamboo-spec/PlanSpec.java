import com.atlassian.bamboo.specs.api.BambooSpec;
import com.atlassian.bamboo.specs.api.builders.BambooKey;
import com.atlassian.bamboo.specs.api.builders.BambooOid;
import com.atlassian.bamboo.specs.api.builders.permission.PermissionType;
import com.atlassian.bamboo.specs.api.builders.permission.Permissions;
import com.atlassian.bamboo.specs.api.builders.permission.PlanPermissions;
import com.atlassian.bamboo.specs.api.builders.plan.Job;
import com.atlassian.bamboo.specs.api.builders.plan.Plan;
import com.atlassian.bamboo.specs.api.builders.plan.PlanIdentifier;
import com.atlassian.bamboo.specs.api.builders.plan.Stage;
import com.atlassian.bamboo.specs.api.builders.plan.branches.BranchCleanup;
import com.atlassian.bamboo.specs.api.builders.plan.branches.PlanBranchManagement;
import com.atlassian.bamboo.specs.api.builders.plan.configuration.ConcurrentBuilds;
import com.atlassian.bamboo.specs.api.builders.plan.dependencies.Dependencies;
import com.atlassian.bamboo.specs.api.builders.project.Project;
import com.atlassian.bamboo.specs.builders.task.CheckoutItem;
import com.atlassian.bamboo.specs.builders.task.MavenTask;
import com.atlassian.bamboo.specs.builders.task.VcsCheckoutTask;
import com.atlassian.bamboo.specs.builders.trigger.RepositoryPollingTrigger;
import com.atlassian.bamboo.specs.util.BambooServer;
import java.time.Duration;

@BambooSpec
public class PlanSpec {

    public Plan plan() {
        final Plan plan = new Plan(new Project()
                .key(new BambooKey("TES1"))
                .name("Test Project")
                .description("Test Project"),
                "Parent Plan",
                new BambooKey("TES1"))
                .description("Parent Plan")
                .pluginConfigurations(new ConcurrentBuilds())
                .stages(new Stage("Default Stage")
                        .jobs(new Job("Default Job",
                                new BambooKey("JOB1"))
                                .tasks(new VcsCheckoutTask()
                                                .description("Checkout Default Repository")
                                                .checkoutItems(new CheckoutItem().defaultRepository()),
                                        new MavenTask()
                                                .description("Maven")
                                                .goal("clean install")
                                                .jdk("JDK 11")
                                                .executableLabel("Maven"))))
                .linkedRepositories("eyella")

                .triggers(new RepositoryPollingTrigger()
                        .withPollingPeriod(Duration.ofSeconds(20)))
                .planBranchManagement(new PlanBranchManagement()
                        .delete(new BranchCleanup())
                        .notificationLikeParentPlan()).dependencies(new Dependencies()
                        .childPlans(new PlanIdentifier("TES1", "CHIL1")))
                .forceStopHungBuilds();
        return plan;
    }

    public PlanPermissions planPermission() {
        final PlanPermissions planPermission = new PlanPermissions(new PlanIdentifier("TES1", "TES1"))
                .permissions(new Permissions()
                        .userPermissions("ryella", PermissionType.EDIT, PermissionType.VIEW_CONFIGURATION, PermissionType.VIEW, PermissionType.ADMIN, PermissionType.CLONE, PermissionType.BUILD));
        return planPermission;
    }

    public static void main(String... argv) {
        //By default credentials are read from the '.credentials' file.
        BambooServer bambooServer = new BambooServer("http://dev4199:8085");
        final PlanSpec planSpec = new PlanSpec();

        final Plan plan = planSpec.plan();
        bambooServer.publish(plan);

        final PlanPermissions planPermission = planSpec.planPermission();
        bambooServer.publish(planPermission);
    }
}