
import json
import os
import pathlib
import requests
from typing import Union


class Preprocessor:
    DATASET_DIRECTORY = pathlib.Path(__file__).parent / "dataset"
    PROCESSED_DIRECTORY = pathlib.Path(__file__).parent / "processed"
    JAVA_DIRECTORY = pathlib.Path(__file__).parent / "java"
    NO_BOT_DIRECTORY = pathlib.Path(__file__).parent / "nobot"
    ATLEAST_ONE_JAVA_FILE = pathlib.Path(__file__).parent / "java_files"

    @classmethod
    def process(
        cls,
        process_directory: pathlib.Path,
        output_directory: pathlib.Path,
        condition,
    ):
        cls._create_directory_safely(output_directory)

        for (root, dirs, files) in os.walk(process_directory):
            if pathlib.Path(root) != process_directory:

                filtered_directory = output_directory / os.path.basename(root)
                cls._create_directory_safely(filtered_directory)

                for file in files:

                    if os.path.exists(filtered_directory / file):
                        continue

                    required_events = []

                    with open(os.path.join(root, file), "r") as data:
                        while True:
                            line = data.readline()
                            if not line:
                                break
                            event = json.loads(line)
                            if condition(event):
                                required_events.append(line)

                    with open(filtered_directory / file, "w") as processed_file:
                        processed_file.writelines(required_events)

    @classmethod
    def _pull_request_event(cls):
        if os.path.exists(cls.PROCESSED_DIRECTORY):
            return

        def pr(event):
            return event["type"] == "PullRequestEvent"

        cls.process(cls.DATASET_DIRECTORY, cls.PROCESSED_DIRECTORY, pr)

    @classmethod
    def _java_project(cls):
        def java(event):
            return (
                event["payload"]["pull_request"]["base"]["repo"]["language"] == "Java"
            )

        cls.process(
            cls.PROCESSED_DIRECTORY,
            cls.JAVA_DIRECTORY,
            java,
        )

    @classmethod
    def _bot_removal(cls):
        def bot(event):
            return "[bot]" not in event["payload"]["pull_request"]["user"]["login"]

        cls.process(cls.JAVA_DIRECTORY, cls.NO_BOT_DIRECTORY, bot)

    @classmethod
    def _atleast_one_java_file(cls):
        def filter(event):
            base_url = event["payload"]["pull_request"]["url"]
            files_url = ""
            if base_url[-1] == "/":
                files_url = f"{base_url}files"
            else:
                files_url = f"{base_url}/files"

            res = requests.get(
                files_url,
                headers={
                    "Authorization": f"token []"
                },
            )

            # The repository might have been deleted
            if res.status_code == 404:
                return False

            if res.status_code == 403:
                raise Exception("API requests limit reached")

            if res.status_code == 422:
                print(f"The weird PR: {files_url}")
                return False

            data = res.json()
            print(files_url)
            for file in data:
                if file["filename"][-5:] == ".java":
                    return True
            return False

        cls.process(cls.NO_BOT_DIRECTORY, cls.ATLEAST_ONE_JAVA_FILE, filter)

    def get_prs(self, dir):
        for (_, _, files) in os.walk(dir):
            for file in files:
                with open(os.path.join(dir, file)) as f:
                    while True:
                        line = f.readline()
                        if not line:
                            break
                        event = json.loads(line)
                        print(event["payload"]["pull_request"]["html_url"])

    @staticmethod
    def _create_directory_safely(directory: Union[pathlib.Path, str]):
        if not os.path.exists(directory):
            os.mkdir(directory)


if __name__ == "__main__":
    P = Preprocessor()
    P.get_prs("/home/assert/Desktop/experiments/badformatting/java_files/2020-01-01")
