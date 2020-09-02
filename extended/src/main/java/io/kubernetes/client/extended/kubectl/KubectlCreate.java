/*
Copyright 2020 The Kubernetes Authors.
Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at
http://www.apache.org/licenses/LICENSE-2.0
Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
*/
package io.kubernetes.client.extended.kubectl;

import com.google.common.base.Strings;
import io.kubernetes.client.Discovery;
import io.kubernetes.client.apimachinery.GroupVersion;
import io.kubernetes.client.common.KubernetesListObject;
import io.kubernetes.client.common.KubernetesObject;
import io.kubernetes.client.extended.kubectl.exception.KubectlException;
import io.kubernetes.client.openapi.ApiException;
import io.kubernetes.client.util.Namespaces;
import io.kubernetes.client.util.generic.GenericKubernetesApi;
import io.kubernetes.client.util.generic.KubernetesApiResponse;
import io.kubernetes.client.util.generic.options.CreateOptions;

public class KubectlCreate extends Kubectl.NamespacedApiClientBuilder<KubectlCreate>
    implements Kubectl.Executable<KubernetesObject> {

  KubectlCreate() {}

  private KubernetesObject targetObj;

  public KubectlCreate resource(KubernetesObject obj) {
    this.targetObj = obj;
    return this;
  }

  @Override
  public KubernetesObject execute() throws KubectlException {
    try {
      Discovery.APIResource apiResource = recognize(this.targetObj);
      GroupVersion gv = GroupVersion.parse(this.targetObj);

      GenericKubernetesApi<KubernetesObject, KubernetesListObject> api =
          new GenericKubernetesApi(
              targetObj.getClass(),
              KubernetesListObject.class,
              gv.getGroup(),
              gv.getVersion(),
              apiResource.getResourcePlural(),
              apiClient);
      if (apiResource.getNamespaced()) {
        String targetNamespace =
            namespace != null
                ? namespace
                : Strings.isNullOrEmpty(targetObj.getMetadata().getNamespace())
                    ? Namespaces.NAMESPACE_DEFAULT
                    : targetObj.getMetadata().getNamespace();

        KubernetesApiResponse<KubernetesObject> response =
            api.create(targetNamespace, targetObj, new CreateOptions());
        return response.getObject();
      } else {
        KubernetesApiResponse<KubernetesObject> response =
            api.create(targetObj, new CreateOptions());
        return response.getObject();
      }
    } catch (ApiException e) {
      throw new KubectlException(e);
    }
  }
}
